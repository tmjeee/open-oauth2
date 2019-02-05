package org.open.oauth2.endpoint;

import org.open.oauth2.Constants;
import org.open.oauth2.interceptor.AuthorizationHeaderInterceptor;
import org.open.oauth2.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping("oauth2")
public class TokenEndPoint implements AuthorizationHeaderInterceptor.AuthorizationHeaderAware {

    private static final Logger logger = LoggerFactory.getLogger(TokenEndPoint.class);

    private String authorizationHeaderUsername;
    private String authorizationHeaderPassword;

    @Autowired
    private DbService dbService;

    @Autowired
    private Encryptor encryptor;

    @Autowired
    private AuthorizationTokenGenerator authorizationTokenGenerator;


    @Override
    public void authorizationHeader(String username, String password) {
        this.authorizationHeaderUsername = username;
        this.authorizationHeaderPassword = password;
    }


    @Transactional(readOnly = true)
    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public ResponseEntity<?> post(
            @RequestParam(value = "grant_type", required = false)String grant_type,
            @RequestParam(value = "code", required = false)String code,
            @RequestParam(value = "redirect_uri", required = false) String redirect_uri,
            @RequestParam(value = "client_id", required = false)String client_id,
            @RequestParam(value = "username", required=false)String username,
            @RequestParam(value = "password", required=false) String password,
            @RequestParam(value = "scope", required =  false) String scope,
            @RequestParam(value = "refresh_token", required=false)String refreshToken
    ) throws NoSuchAlgorithmException {

        if (Constants.GRANT_TYPE_VALUE_AUTHORIZATION_CODE.equals(grant_type)) {
            /**
             *  - grant_type  'authorization_code'
             *  - code
             *  - redirect_uri (optional)
             *  - client_id (optional, only when BASIC authorization header is not provided and client is non-confidential)
             *  - BASIC Authorization header (optional, if client is non-confidential
             */
            return handleGrantTypeAuthorizationCode( code, redirect_uri, client_id);
        } else if (Constants.GRANT_TYPE_VALUE_CLIENT_CREDENTIALS.equals(grant_type)) {
            /**
             *  - grant_type 'client_credentials'
             *  - scope
             *  - BASIC Authorization header
             */
            return handleGrantTypeClientCredentials(scope);
        } else if (Constants.GRANT_TYPE_VALUE_PASSWORD.equals(grant_type)) {
            /**
             *  - grant_type 'password'
             *  - username
             *  - password
             *  - scope (optional)
             *  - BASIC Authorization header
             */
            return handleGrantTypePassword(username, password, scope);
        } else if (Constants.GRANT_TYPE_VALUE_REFRESH_TOKEN.equals(grant_type)) {
            /**
             *  - grant_type 'refresh_token'
             *  - refresh_token
             *  - scope (optional)
             *  - BASIC Authorization header
             */
            return handleGrantTypeRefreshToken(refreshToken, scope);
        }

        return new ResponseEntity<>(
                new Oauth2ServerError(
                        Constants.ERROR_UNSUPPORTED_GRANT_TYPE,
                        String.format("Grant type %s is not supported", grant_type)),
                HttpStatus.BAD_REQUEST);
    }

    //////////////////////  private

    private void verifyBasicAuthentication(String client_id, Consumer<ResponseEntity<?>> error, Consumer<Oauth2Client> success) throws NoSuchAlgorithmException {
        // Basic authentication provided
        Oauth2Client oauth2Client = null;
        if (!StringUtils.isEmpty(authorizationHeaderUsername)) {
            logger.error("**************** "+authorizationHeaderUsername+"("+authorizationHeaderPassword+")");
            oauth2Client = dbService.findClient(authorizationHeaderUsername);
            if ((oauth2Client == null) ||
                    (!oauth2Client.client_secret.equals(encryptor.digest(authorizationHeaderPassword)))) {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.set(HttpHeaders.WWW_AUTHENTICATE, "Basic");
                error.accept(new ResponseEntity<Oauth2ServerError>(
                        new Oauth2ServerError(
                                Constants.ERROR_INVALID_CLIENT,
                                String.format("Client %s not found or invalid password", authorizationHeaderUsername)),
                        httpHeaders,
                        HttpStatus.UNAUTHORIZED));
                return;
            }
            // no basic authentication provided
        } else {
            // no basic authentication provided but client_id is given
            if (!StringUtils.isEmpty(client_id)) {
                oauth2Client = dbService.findClient(client_id);
                if (oauth2Client != null && oauth2Client.confidential) {
                    error.accept(new ResponseEntity<> (
                            new Oauth2ServerError(
                                    Constants.ERROR_ACCESS_DENIED,
                                    String.format("Confidential client must authenticate through Basic Authorization Header")),
                            HttpStatus.UNAUTHORIZED));
                    return;
                }
            }
        }

        if (oauth2Client == null) {
            error.accept(new ResponseEntity<>(
                    new Oauth2ServerError(
                            Constants.ERROR_INVALID_CLIENT,
                            String.format("No client authentication found")),
                    HttpStatus.UNAUTHORIZED));
            return;
        }
        success.accept(oauth2Client);
    }

    /////////////////////  protected hooks

    /**
     *  - grant_type  'authorization_code'
     *  - code
     *  - redirect_uri (optional)
     *  - client_id (optional, only when BASIC authorization header is not provided and client is non-confidential)
     *  - BASIC Authorization header (optional, if client is non-confidential
     *
     * @param code
     * @param redirect_uri
     * @param client_id
     * @return
     * @throws NoSuchAlgorithmException
     */
    protected ResponseEntity<?> handleGrantTypeAuthorizationCode(
            String code,
            String redirect_uri,
            String client_id) throws NoSuchAlgorithmException {

        // Basic authentication provided
        Oauth2Client[] oauth2Client = new Oauth2Client[1];
        ResponseEntity<?>[] responseEntity = new ResponseEntity[1];
        verifyBasicAuthentication(
                client_id,
                (ResponseEntity<?> r)-> {
                    responseEntity[0] = r;
                },
                (Oauth2Client c)->{
                    oauth2Client[0] = c;
                });

        if (responseEntity[0] != null) {
            return responseEntity[0];
        }

        if (!oauth2Client[0].grant_types.contains(Constants.GRANT_TYPE_VALUE_AUTHORIZATION_CODE)) {
           return new ResponseEntity<>(
                   new Oauth2ServerError(
                           Constants.ERROR_INVALID_GRANT,
                           "Grant type 'authorization_code' is not given for this client"
                   ),
                   HttpStatus.OK);
        }

        Oauth2AuthorizationCode oauth2AuthorizationCode = dbService.findAuthorizationCode(code);
        if (oauth2AuthorizationCode == null) {
            return new ResponseEntity<>(
              new Oauth2ServerError(
                    Constants.ERROR_INVALID_GRANT,
                    "Invalid code provided"
              ),
              HttpStatus.OK
            );
        }
        if ((!StringUtils.isEmpty(oauth2AuthorizationCode.provided_redirect_uri)) &&
            (!oauth2AuthorizationCode.provided_redirect_uri.equals(redirect_uri))) {
            return new ResponseEntity<>(
                    new Oauth2ServerError(
                            Constants.ERROR_INVALID_REQUEST,
                            String.format("")),
                    HttpStatus.OK);
        }
        Oauth2ResourceOwner oauth2ResourceOwner = dbService.findResourceOwner(oauth2AuthorizationCode.resource_owner_username);

        AuthorizationTokenGenerator.TokenGenerated tokenGenerated = authorizationTokenGenerator.generateToken(
                oauth2Client[0],
                oauth2ResourceOwner,
                oauth2AuthorizationCode.scopes,
                true);

        dbService.deleteAuthorizationCode(oauth2AuthorizationCode.id);

        return new ResponseEntity<>(
                new Oauth2AuthorizationTokenResponseSuccess(
                        tokenGenerated.authorization_token,
                        tokenGenerated.token_type,
                        tokenGenerated.expires_in,
                        tokenGenerated.refresh_token,
                        tokenGenerated.scope.stream().collect(Collectors.joining(" "))
                ),
                HttpStatus.OK);
    }

    /**
     *  - grant_type 'client_credentials'
     *  - scope
     *  - BASIC Authorization header
     *
     * @param scope
     * @return
     * @throws NoSuchAlgorithmException
     */
    protected ResponseEntity<?> handleGrantTypeClientCredentials(
            String scope) throws NoSuchAlgorithmException {

        Oauth2Client[] _oauth2Client = new Oauth2Client[1] ;
        ResponseEntity<?>[] responseEntity = new ResponseEntity<?>[1];
        verifyBasicAuthentication(
                null,
                (ResponseEntity<?> r)->{
                    responseEntity[0] = r;
                },
                (Oauth2Client c)->{
                    _oauth2Client[0] = c;
                });
        if (responseEntity[0] != null) {
            return responseEntity[0];
        }

        List<String> scopes = StringUtils.isEmpty(scope) ? _oauth2Client[0].scopes : Arrays.asList(scope.split(" "));

        if (!_oauth2Client[0].grant_types.contains(Constants.GRANT_TYPE_VALUE_CLIENT_CREDENTIALS)) {
           return new ResponseEntity<>(
                   new Oauth2ServerError(
                           Constants.ERROR_INVALID_GRANT,
                           "Grant type 'client_credentials' is not given to this client"),
                   HttpStatus.OK);
        }

        if (!_oauth2Client[0].scopes.containsAll(scopes)) {
            scopes = new ArrayList<>(scopes);
            scopes.retainAll(_oauth2Client[0].scopes);
        }

        AuthorizationTokenGenerator.TokenGenerated tokenGenerated =
                authorizationTokenGenerator.generateToken(
                        _oauth2Client[0],
                        null,
                        scopes,
                        false);

        return new ResponseEntity<>(
                new Oauth2AuthorizationTokenResponseSuccess(
                        tokenGenerated.authorization_token,
                        tokenGenerated.token_type,
                        tokenGenerated.expires_in,
                        tokenGenerated.refresh_token,
                        scope),
                HttpStatus.OK);
    }

    /**
     *  - grant_type 'password'
     *  - username
     *  - password
     *  - scope (optional)
     *  - BASIC Authorization header
     *
     * @param username
     * @param password
     * @param scope
     * @return
     * @throws NoSuchAlgorithmException
     */
    protected ResponseEntity<?> handleGrantTypePassword(
            String username,  // resouce owner username
            String password,  // resource owner password
            String scope) throws NoSuchAlgorithmException {

        Oauth2Client[] oauth2Client = new Oauth2Client[1] ;
        ResponseEntity<?>[] responseEntity = new ResponseEntity[1];
        verifyBasicAuthentication(
                null,
                (ResponseEntity<?> r)->{
                    responseEntity[0] = r;
                },
                (Oauth2Client c)->{
                    oauth2Client[0] = c;
                });
        if (responseEntity[0] != null) {
            return responseEntity[0];
        }


        List<String> scopes = StringUtils.isEmpty(scope) ? oauth2Client[0].scopes : Arrays.asList(scope.split(" "));
        Oauth2ResourceOwner oauth2ResourceOwner = dbService.findResourceOwner(username);
        if (oauth2ResourceOwner == null ||
           (!oauth2ResourceOwner.password.equals(encryptor.digest(password)))) {
            return new ResponseEntity<>(
                    new Oauth2ServerError(
                            Constants.ERROR_INVALID_REQUEST,
                            "Resource owner failed authentication"),
                    HttpStatus.OK);
        }

        if (!oauth2Client[0].grant_types.contains(Constants.GRANT_TYPE_VALUE_PASSWORD)) {
            return new ResponseEntity<>(
                    new Oauth2ServerError(
                            Constants.ERROR_INVALID_GRANT,
                            "Grant type 'password' is not given to this client"),
                    HttpStatus.OK);
        }

        if (!oauth2Client[0].scopes.containsAll(scopes)) {
            scopes = new ArrayList<>(scopes);
            scopes.retainAll(oauth2Client[0].scopes);
        }


        AuthorizationTokenGenerator.TokenGenerated tokenGenerated =
                authorizationTokenGenerator.generateToken(
                        oauth2Client[0],
                        oauth2ResourceOwner,
                        scopes,
                        false);


        return new ResponseEntity<>(
                new Oauth2AuthorizationTokenResponseSuccess(
                        tokenGenerated.authorization_token,
                        tokenGenerated.token_type,
                        tokenGenerated.expires_in,
                        tokenGenerated.refresh_token,
                        tokenGenerated.scope.stream().collect(Collectors.joining(" "))
                ),
                HttpStatus.OK
        );
    }

    /**
     *  - grant_type 'refresh_token'
     *  - refresh_token
     *  - scope (optional)
     *  - BASIC Authorization header
     *
     * @param refresh_token
     * @param scope
     * @return
     * @throws NoSuchAlgorithmException
     */
    protected ResponseEntity<?> handleGrantTypeRefreshToken(
            String refresh_token,
            String scope) throws NoSuchAlgorithmException {

        Oauth2Client[] oauth2Client = new Oauth2Client[1];
        ResponseEntity<?>[] responseEntity = new ResponseEntity[1];
        verifyBasicAuthentication(
                null,
                (ResponseEntity<?> r)->{
                    responseEntity[0] = r;
                },
                (Oauth2Client c)->{
                    oauth2Client[0] = c;
                });
        if (responseEntity[0] != null) {
            return responseEntity[0];
        }


        List<String> scopes = StringUtils.isEmpty(scope) ? oauth2Client[0].scopes : Arrays.asList(scope.split(" "));
        if (!oauth2Client[0].grant_types.contains(Constants.GRANT_TYPE_VALUE_REFRESH_TOKEN)) {
            return new ResponseEntity<>(
                    new Oauth2ServerError(
                            Constants.ERROR_INVALID_GRANT,
                            "Grant type 'refresh_token' is not given to this client"),
                    HttpStatus.OK);
        }

        if (!oauth2Client[0].scopes.containsAll(scopes)) {
            scopes = new ArrayList<>(scopes);
            scopes.retainAll(oauth2Client[0].scopes);
        }

        Oauth2AuthorizationToken oauth2AuthorizationToken = dbService.findAuthorizationTokenByRefreshToken(refresh_token);
        if (oauth2AuthorizationToken == null){
            return new ResponseEntity<>(
                    new Oauth2ServerError(
                            Constants.ERROR_INVALID_REQUEST,
                            "Invalid refresh token"),
                    HttpStatus.OK);
        }

        Oauth2ResourceOwner oauth2ResourceOwner = dbService.findResourceOwner(oauth2AuthorizationToken.resource_owner_username);

        AuthorizationTokenGenerator.TokenGenerated tokengenerated =
                authorizationTokenGenerator.generateToken(oauth2Client[0], oauth2ResourceOwner, scopes, true);

        dbService.deleteAuthorizationToken(oauth2AuthorizationToken.id);

        return new ResponseEntity<>(
                new Oauth2AuthorizationTokenResponseSuccess(
                        tokengenerated.authorization_token,
                        tokengenerated.token_type,
                        tokengenerated.expires_in,
                        tokengenerated.refresh_token,
                        tokengenerated.scope.stream().collect(Collectors.joining(" "))
                ),
                HttpStatus.OK);

    }
}
