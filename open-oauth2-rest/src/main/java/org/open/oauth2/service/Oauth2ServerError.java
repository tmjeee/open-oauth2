package org.open.oauth2.service;

public class Oauth2ServerError {
    public final String error;
    public final String description;
    public Oauth2ServerError(String error, String description) {
        this.error = error;
        this.description = description;
    }
}
