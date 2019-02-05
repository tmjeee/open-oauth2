package org.open.oauth2.service;

public class Oauth2AuthorizationTokenResponseSuccess {

    public final String access_token;
    public final String token_type;
    public final Long expires_in;
    public final String refresh_token;
    public final String scope;

    public Oauth2AuthorizationTokenResponseSuccess(String access_token,
                            String token_type,
                            Long expires_in,
                            String refresh_token,
                            String scope) {
        this.access_token = access_token;
        this.token_type = token_type;
        this.expires_in = expires_in;
        this.refresh_token = refresh_token;
        this.scope = scope;
    }
}
