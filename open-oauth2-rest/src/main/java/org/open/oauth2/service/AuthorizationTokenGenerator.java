package org.open.oauth2.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public abstract class AuthorizationTokenGenerator {

    public static class TokenGenerated {
        public final String authorization_token;
        public final String refresh_token;
        public final Long expires_in; // lifetime in seconds
        public final String token_type;
        public final List<String> scope;
        TokenGenerated(String authorization_token, String refresh_token,
                       Long expires_in, String token_type, List<String> scope) {
            this.authorization_token = authorization_token;
            this.refresh_token = refresh_token;
            this.expires_in = expires_in;
            this.token_type = token_type;
            this.scope = Collections.unmodifiableList(scope);
        }
    }

    @Autowired
    private DbService dbService;


    public TokenGenerated generateToken(Oauth2Client oauth2Client,
                                        Oauth2ResourceOwner oauth2User,
                                        List<String> scopes,
                                        boolean generateRefreshToken) {

        TokenGenerated tokenGenerated =  new TokenGenerated(
                createAccessToken(oauth2Client),
                (generateRefreshToken?createRefreshToken(oauth2Client):null),
                oauth2Client.token_expiration_in_seconds,
                "Bearer",
                scopes
        );

        LocalDateTime expirationDate = LocalDateTime.now().plus(oauth2Client.token_expiration_in_seconds, ChronoUnit.SECONDS);
        LocalDateTime creationDate = LocalDateTime.now();

        dbService.deleteAuthorizationTokenByClientAndResourceOwner(oauth2Client.id, (oauth2User == null ? null : oauth2User.id));
        dbService.insertAuthorizationToken(
                oauth2Client.id,
                (oauth2User != null ? oauth2User.id : null),
                scopes,
                tokenGenerated.authorization_token,
                tokenGenerated.refresh_token,
                expirationDate, creationDate);


        return tokenGenerated;
    }

    protected abstract String createAccessToken(Oauth2Client oauth2Client);
    protected abstract String createRefreshToken(Oauth2Client oauth2Client);
}
