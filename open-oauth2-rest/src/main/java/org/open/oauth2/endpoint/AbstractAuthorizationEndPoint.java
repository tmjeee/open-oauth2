package org.open.oauth2.endpoint;

import org.open.oauth2.Constants;
import org.open.oauth2.Utils;
import org.open.oauth2.config.OpenOAuth2ConfigurationProperties;
import org.open.oauth2.service.AuthorizationCodeGenerator;
import org.open.oauth2.service.AuthorizationTokenGenerator;
import org.open.oauth2.service.Oauth2Client;
import org.open.oauth2.service.Oauth2ResourceOwner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AbstractAuthorizationEndPoint {

    @Autowired
    private OpenOAuth2ConfigurationProperties configurationProperties;


    @Autowired
    private AuthorizationCodeGenerator authorizationCodeGenerator;

    @Autowired
    private AuthorizationTokenGenerator authorizationTokenGenerator;



    protected ResponseEntity<?> redirectCodeOrTokenToClient(HttpServletResponse response,
                                                            String response_type,
                                                            String effective_redirect_uri,  // worked out by oauth2 server
                                                            String redirect_uri,            // provided by client (might be empty if not provided)
                                                            String state,
                                                            List<String> scopes,
                                                            Oauth2Client oauth2Client,
                                                            Oauth2ResourceOwner oauth2User) throws IOException {
        // actual work
        if (Constants.RESPONSE_TYPE_CODE.equals(response_type)) {
            // redirect with access code
            String authorizationCode = authorizationCodeGenerator.generateCode(oauth2Client, oauth2User,
                    effective_redirect_uri, redirect_uri, scopes);
            Utils.redirectAuthorizationCodeSuccess(response,
                    effective_redirect_uri,
                    authorizationCode,
                    state);
            return null;
        } else if (Constants.RESPONSE_TYPE_TOKEN.equals(response_type)) {
            // redirect with access token
            AuthorizationTokenGenerator.TokenGenerated authorizationToken = authorizationTokenGenerator.generateToken(
                    oauth2Client, oauth2User, scopes, false);
            Utils.redirectAuthorizationTokenSuccess(response,
                    effective_redirect_uri,
                    authorizationToken,
                    scopes,
                    state);
            return null;
        }
        return null;
    }
}
