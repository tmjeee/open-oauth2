package org.open.oauth2.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Oauth2Client {

    public final long id;
    public final String client_id;
    @JsonIgnore
    public final String client_secret;
    public final String email;
    public final String description;
    public final boolean confidential;
    public final long token_expiration_in_seconds;
    public final LocalDateTime creationDate;
    public final List<String> grant_types;
    public final List<String> redirect_uris;
    public final List<String> scopes;

    public Oauth2Client(
            long id,
            String client_id,
            String client_secret,
            String email,
            String description,
            boolean confidential,
            long token_expiration_in_seconds,
            LocalDateTime creationDate,
            List<String> grant_types,
            List<String> redirect_uris,
            List<String> scopes
    ) {
        this.id = id;
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.email = email;
        this.description = description;
        this.confidential = confidential;
        this.token_expiration_in_seconds = token_expiration_in_seconds;
        this.creationDate = creationDate;
        this.grant_types = grant_types;
        this.redirect_uris = redirect_uris;
        this.scopes = scopes;
    }
}


class MutableOauth2Client {
    public long id;
    public String client_id;
    public String client_secret;
    public String email;
    public String description;
    public boolean confidential;
    public LocalDateTime creation_date;
    public long token_expiration_in_seconds;
    public List<String> grant_types = new ArrayList<>();
    public List<String> redirect_uris = new ArrayList<>();
    public List<String> scopes = new ArrayList<>();
}


class Oauth2ClientRowReducer implements RowReducer<Map<Integer, MutableOauth2Client>, Oauth2Client> {

    @Override
    public Map<Integer, MutableOauth2Client> container() {
        return new LinkedHashMap<>();
    }

    @Override
    public void accumulate(Map<Integer,MutableOauth2Client> container, RowView rowView) {
        container.compute(
                rowView.getColumn("id", Integer.class),
                (Integer id, MutableOauth2Client c)->{
                    if (c == null) {
                        c = new MutableOauth2Client();
                        c.id = rowView.getColumn("id", Integer.class);
                        c.client_id = rowView.getColumn("client_id", String.class);
                        c.client_secret = rowView.getColumn("client_secret", String.class);
                        c.email = rowView.getColumn("email", String.class);
                        c.description = rowView.getColumn("description", String.class);
                        c.confidential = rowView.getColumn("confidential", Boolean.class);
                        c.creation_date = rowView.getColumn("creation_date", LocalDateTime.class);
                        c.token_expiration_in_seconds = rowView.getColumn("token_expiration_in_seconds", Long.class);
                    }
                    String grant_type = rowView.getColumn("grant_type", String.class);
                    if (!c.grant_types.contains(grant_type)) {
                        c.grant_types.add(grant_type);
                    }
                    String redirect_uri = rowView.getColumn("redirect_uri", String.class);
                    if (!c.redirect_uris.contains(redirect_uri)) {
                        c.redirect_uris.add(redirect_uri);
                    }
                    String scope = rowView.getColumn("scope", String.class);
                    if (!c.scopes.contains(scope)) {
                        c.scopes.add(rowView.getColumn("scope", String.class));
                    }
                    return c;
                });
    }

    @Override
    public Stream<Oauth2Client> stream(Map<Integer, MutableOauth2Client> container) {
        return container.values().stream().map(
                        (MutableOauth2Client c)->new Oauth2Client(
                                c.id,
                                c.client_id,
                                c.client_secret,
                                c.email,
                                c.description,
                                c.confidential,
                                c.token_expiration_in_seconds,
                                c.creation_date,
                                c.grant_types,
                                c.redirect_uris,
                                c.scopes
                        )
                ).collect(Collectors.toList()).stream();
    }
}

