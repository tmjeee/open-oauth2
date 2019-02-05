package org.open.oauth2.service;

import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Oauth2AuthorizationToken {

    public final Long id;
    public final String authorization_token;
    public final String refresh_token;
    public final LocalDateTime expire_date;
    public final LocalDateTime creation_date;
    public final Long client_uid;
    public final String client_id;
    public final Long client_token_expiration_in_seconds;
    public final String client_description;
    public final Boolean client_confidential;
    public final LocalDateTime client_creation_date;
    public final Long resource_owner_id;
    public final String resource_owner_username;
    public final Boolean resource_owner_auto_approve_scope;
    public final LocalDateTime resource_owner_creation_date;
    public final List<String> scopes;


    Oauth2AuthorizationToken(MutableOauth2AuthorizationToken m) {
        this.id = m.id;
        this.authorization_token = m.authorization_token;
        this.refresh_token = m.refresh_token;
        this.expire_date = m.expire_date;
        this.creation_date = m.creation_date;
        this.client_uid = m.client_uid;
        this.client_id = m.client_id;
        this.client_token_expiration_in_seconds = m.client_token_expiration_in_seconds;
        this.client_description = m.client_description;
        this.client_confidential = m.client_confidential;
        this.client_creation_date = m.client_creation_date;
        this.resource_owner_id = m.resource_owner_id;
        this.resource_owner_username = m.resource_owner_username;
        this.resource_owner_auto_approve_scope = m.resource_owner_auto_approve_scope;
        this.resource_owner_creation_date = m.resource_owner_creation_date;
        this.scopes = Collections.unmodifiableList(m.scopes);
    }
}

class MutableOauth2AuthorizationToken {
    public Long id;
    public String authorization_token;
    public String refresh_token;
    public LocalDateTime expire_date;
    public LocalDateTime creation_date;
    public Long client_uid;
    public String client_id;
    public Long client_token_expiration_in_seconds;
    public String client_description;
    public Boolean client_confidential;
    public LocalDateTime client_creation_date;
    public Long resource_owner_id;
    public String resource_owner_username;
    public Boolean resource_owner_auto_approve_scope;
    public LocalDateTime resource_owner_creation_date;
    public List<String> scopes = new ArrayList<>();
}


class Oauth2AuthorizationTokenRowReducer implements RowReducer<Map<Long, MutableOauth2AuthorizationToken>, Oauth2AuthorizationToken> {

    @Override
    public Map<Long, MutableOauth2AuthorizationToken> container() {
        return new LinkedHashMap<>();
    }

    @Override
    public void accumulate(Map<Long, MutableOauth2AuthorizationToken> container, RowView rowView) {
        container.compute(
                rowView.getColumn("tokenId", Long.class),
                (Long tokenId, MutableOauth2AuthorizationToken token)->{
                    if (token == null) {
                        token = new MutableOauth2AuthorizationToken();
                        token.id = rowView.getColumn("tokenId", Long.class);
                        token.authorization_token = rowView.getColumn("authorizationToken", String.class);
                        token.refresh_token = rowView.getColumn("refreshToken", String.class);
                        token.expire_date = rowView.getColumn("expireDate", LocalDateTime.class);
                        token.creation_date = rowView.getColumn("creationDate", LocalDateTime.class);
                        token.client_uid = rowView.getColumn("clientUid", Long.class);
                        token.client_id = rowView.getColumn("clientId", String.class);
                        token.client_token_expiration_in_seconds = rowView.getColumn("clientTokenExpirationInSeconds", Long.class);
                        token.client_description = rowView.getColumn("clientDescription", String.class);
                        token.client_confidential = rowView.getColumn("clientConfidential", Boolean.class);
                        token.client_creation_date = rowView.getColumn("clientCreationDate", LocalDateTime.class);
                        token.resource_owner_id = rowView.getColumn("resourceOwnerId", Long.class);
                        token.resource_owner_username = rowView.getColumn("resourceOwnerUsername", String.class);
                        token.resource_owner_auto_approve_scope = rowView.getColumn("resourceOwnerAutoApproveScope", Boolean.class);
                        token.resource_owner_creation_date = rowView.getColumn("resourceOwnerCreationDate", LocalDateTime.class);
                    }
                    token.scopes.add(rowView.getColumn("scope",String.class));
                    return token;
                });
    }

    @Override
    public Stream<Oauth2AuthorizationToken> stream(Map<Long, MutableOauth2AuthorizationToken> container) {
        return container
                .values()
                .stream()
                .map((MutableOauth2AuthorizationToken t)->new Oauth2AuthorizationToken(t))
                .collect(Collectors.toList())
                .stream();
    }
}