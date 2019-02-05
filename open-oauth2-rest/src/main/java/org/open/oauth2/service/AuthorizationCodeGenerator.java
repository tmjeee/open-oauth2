package org.open.oauth2.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

public abstract class AuthorizationCodeGenerator {


    @Autowired
    private DbService dbService;

    public String generateCode(Oauth2Client oauth2Client,
                               Oauth2ResourceOwner oauth2User,
                               String effective_redirect_uri, // worked out by oauth2 server
                               String redirect_uri, // the one provided by client (might be empty is not provided)
                               List<String> scope) {
        String authorizationCode = createAuthenticationCode(oauth2Client);
        dbService.deleteAuthorizationCodeByClientAndResourceOwner(oauth2Client.id, oauth2User.id);
        dbService.insertAuthorizationCode(
                oauth2Client.id,
                oauth2User.id,
                effective_redirect_uri,
                redirect_uri,
                scope,
                authorizationCode,
                LocalDateTime.now());
        return authorizationCode;
    }

    protected abstract String createAuthenticationCode(Oauth2Client oauth2Client);
}
