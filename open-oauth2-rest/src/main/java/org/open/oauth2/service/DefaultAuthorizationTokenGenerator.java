package org.open.oauth2.service;

import java.util.UUID;

public class DefaultAuthorizationTokenGenerator extends AuthorizationTokenGenerator {

    @Override
    protected String createAccessToken(Oauth2Client oauth2Client) {
       return UUID.randomUUID().toString();
    }

    @Override
    protected String createRefreshToken(Oauth2Client oauth2Client) {
        return UUID.randomUUID().toString();
    }
}
