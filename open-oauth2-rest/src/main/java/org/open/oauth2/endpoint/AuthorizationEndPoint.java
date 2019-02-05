package org.open.oauth2.endpoint;

import org.open.oauth2.Constants;
import org.open.oauth2.Utils;
import org.open.oauth2.config.OpenOAuth2ConfigurationProperties;
import org.open.oauth2.interceptor.AuthorizationHeaderInterceptor;
import org.open.oauth2.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("oauth2")
public class AuthorizationEndPoint extends AbstractAuthorizationEndPoint implements
        AuthorizationHeaderInterceptor.AuthorizationHeaderAware {

    @Autowired
    private OpenOAuth2ConfigurationProperties configurationProperties;

    @Autowired
    private DbService dbService;

    @Autowired
    private AuthorizationCodeGenerator authorizationCodeGenerator;

    @Autowired
    private AuthorizationTokenGenerator authorizationTokenGenerator;

    @Autowired
    private Encryptor encryptor;

    private String authorizationHeaderUsername;
    private String authorizationHeaderPassword;

    @Override
    public void authorizationHeader(String username, String password) {
        this.authorizationHeaderUsername = username;
        this.authorizationHeaderPassword = password;
    }


    /**
     * Require (as request paramter) :-
     * <ul>
     *     <li>client_id</li>
     *     <li>response_type</li>
     *     <li>redirect_uri (optional)</li>
     *     <li>scope (optional) </li>
     *     <li>state (optional) </li>
     * </ul>
     *
     * Require as Basic Authentication Header
     * <ul>
     *     <li>Resource Owner credentials (optional)</li>
     * </ul>
     *
     * @param clientId
     * @param response_type
     * @param redirect_uri
     * @param scope
     * @param state
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchPaddingException
     */
    @Transactional
    @RequestMapping(value = "/authorize", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> getOrPost(
            @RequestParam(value = "client_id", required = false)String clientId,
            @RequestParam(value = "response_type", required = false) String response_type,
            @RequestParam(value = "redirect_uri", required = false) String redirect_uri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required=false)String state,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException,
      InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException,
      NoSuchPaddingException {


        /*
         * - any redirects means it goes to the client
         * - any return of ResponseEntity (non-redirect) means it goes to the Resource Owner
         */
        String effective_redirect_uri = redirect_uri;

        // validate client_id
        if (StringUtils.isEmpty(clientId)) {
            Oauth2ServerError error = new Oauth2ServerError(Constants.ERROR_INVALID_REQUEST, "Missing client_id");
            return new ResponseEntity<>(error, HttpStatus.OK);
        }


        // redirect_uri
        List<String> redirect_uris =  dbService.findAuthorizationRedirectURIs(clientId);
        if (StringUtils.isEmpty(effective_redirect_uri)) {
            if (redirect_uris.size() > 0) { // no redirect_uri passed in use a configured one
                effective_redirect_uri = redirect_uris.get(0);
            } else { // no redirect_uri given and none configured
                Oauth2ServerError error = new Oauth2ServerError(Constants.ERROR_INVALID_REQUEST, String.format("No redirect_uri request parameter and none configured with client %s", clientId));
                return new ResponseEntity<>(error, HttpStatus.OK);
            }
        } else { // redirect_uri being passed in
            // response_type=token, section 4.2.1 MUST not allow if redirect_uri is not a pre-registered one
            // response_type=code, section 4.1.2.1 'implies' that redriect_uri passed in needs to pre-registered
            //                     since it expect error for 'mismatching redirection URI'

            String tmp_uri = effective_redirect_uri;
            Optional<String> uri_found = redirect_uris.stream().filter((String uri)->uri.equals(tmp_uri)).findAny();
            if (!uri_found.isPresent()) {
                Oauth2ServerError error = new Oauth2ServerError(Constants.ERROR_INVALID_REQUEST, String.format("redirect_uri request parameter do not match pre-registered ones"));
                return new ResponseEntity<>(error, HttpStatus.OK);
            }
        }

        // validate response_type
        if (StringUtils.isEmpty(response_type)) {
            handleError(response, response_type, effective_redirect_uri, state, Constants.ERROR_UNSUPPORTED_RESPONSE_TYPE);
            return null;
        }
        if ((!Constants.RESPONSE_TYPE_CODE.equals(response_type)) && (!Constants.RESPONSE_TYPE_TOKEN.equals(response_type))) {
            handleError(response, response_type, effective_redirect_uri, state, Constants.ERROR_UNSUPPORTED_RESPONSE_TYPE);
            return null;
        }


        // verify client
        Oauth2Client oauth2Client = dbService.findClient(clientId);
        if (oauth2Client == null) {
            handleError(response, response_type, effective_redirect_uri, state, Constants.ERROR_UNAUTHORIZED_CLIENT);
            return null;
        }


        // validate scopes
        List<String> scopes = ((StringUtils.isEmpty(scope))? (new ArrayList()) : Arrays.asList(scope.split(" ")));
        if (scopes.isEmpty()) {
           scopes = oauth2Client.scopes;
        } else {
            String invalidScopes = "";
            for (String _scope:scopes) {
                if (!oauth2Client.scopes.contains(_scope)) {
                    invalidScopes = invalidScopes + _scope + " ";
                }
            }
            if (!StringUtils.isEmpty(invalidScopes)) {
                handleError(response, response_type, effective_redirect_uri, state, Constants.ERROR_INVALID_SCOPE);
                return null;
            }
        }


        // is resource owner authenticated?
        if (authorizationHeaderUsername == null) {
            // not authenticated through Authorization:Basic
            // request authentication from resource owner (through redirection) asking for username & password
            TransactionIdMap m = new TransactionIdMap();
            m.setEntryClientId(clientId);
            m.setEntryResponseType(response_type);
            m.setEntryScope(scopes.stream().collect(Collectors.joining(" ")));
            m.setEntryState(state);
            m.setEntryEffectiveRedirectUri(effective_redirect_uri);
            m.setEntryRedirectUri(redirect_uri);
            String stringContent = m.toStringContent();
            String encryptedContent = encryptor.encrypt(stringContent);
            String transactionId = UUID.randomUUID().toString();

            dbService.insertOrUpdateTransaction(transactionId, encryptedContent);

            Utils.redirectWithTransactionId(
              response,
              configurationProperties.getWebconsole().getServer()+
              configurationProperties.getWebconsole().getResourceOwnerAuthentication(),
              transactionId
            );
            return null;
        }
        Oauth2ResourceOwner oauth2User = dbService.findResourceOwner(authorizationHeaderUsername);
        String pwd = encryptor.digest(authorizationHeaderPassword);
        if (oauth2User == null || (!oauth2User.password.equals(pwd))) {
            handleError(response, response_type, effective_redirect_uri, state, Constants.ERROR_ACCESS_DENIED);
            return null;
        }

        // actual work
        redirectCodeOrTokenToClient(
                response,
                response_type,
                effective_redirect_uri,  // worked out by oauth2 server
                redirect_uri,            // provided by client (might be empty if not provided)
                state,
                scopes,
                oauth2Client,
                oauth2User
        );

        return null;
    }

    private void handleError(HttpServletResponse response, String response_type, String redirect_uri,
                             String state, String error) throws IOException {
        if (Constants.RESPONSE_TYPE_CODE.equals(response_type)) {
            Utils.redirectAuthorizationCodeError(response,
                    redirect_uri,
                    error,
                    state);
        } else if (Constants.RESPONSE_TYPE_TOKEN.equals(response_type)) {
            Utils.redirectAuthorizationTokenError(response,
                    redirect_uri,
                    error,
                    state);
        }
    }
}
