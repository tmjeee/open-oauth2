package org.open.oauth2.service;

import java.util.UUID;

public class DefaultAuthorizationCodeGenerator extends AuthorizationCodeGenerator {

    @Override
    protected String createAuthenticationCode(Oauth2Client oauth2Client) {
        return UUID.randomUUID().toString();
    }
}
