package org.open.oauth2.service;

import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Oauth2AuthorizationCode {
    public final Long id;
    public final String code;
    public final LocalDateTime creation_date;
    public final String effective_redirect_uri;
    public final String provided_redirect_uri;
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

    Oauth2AuthorizationCode(MutableOauth2AuthorizationCode c) {
        this.id = c.id;
        this.code = c.code;
        this.creation_date = c.creation_date;
        this.effective_redirect_uri = c.effective_redirect_uri;
        this.provided_redirect_uri = c.provided_redirect_uri;
        this.client_uid = c.client_uid;
        this.client_id = c.client_id;
        this.client_token_expiration_in_seconds = c.client_token_expiration_in_seconds;
        this.client_description = c.client_description;
        this.client_confidential = c.client_confidential;
        this.client_creation_date = c.client_creation_date;
        this.resource_owner_id = c.resource_owner_id;
        this.resource_owner_username = c.resource_owner_username;
        this.resource_owner_auto_approve_scope = c.resource_owner_auto_approve_scope;
        this.resource_owner_creation_date = c.resource_owner_creation_date;
        this.scopes = Collections.unmodifiableList(c.scopes);
    }
}

class MutableOauth2AuthorizationCode {
    public Long id;
    public String code;
    public LocalDateTime creation_date;
    public String effective_redirect_uri;
    public String provided_redirect_uri;
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
    public List<String> scopes= new ArrayList<>();
}

class Oauth2AuthorizationCodeRowReducer implements RowReducer<Map<Long,MutableOauth2AuthorizationCode>, Oauth2AuthorizationCode> {

    @Override
    public Map<Long, MutableOauth2AuthorizationCode> container() {
        return new LinkedHashMap<>();
    }

    @Override
    public void accumulate(Map<Long, MutableOauth2AuthorizationCode> container, RowView rowView) {
        container.compute(
                rowView.getColumn("codeId", Long.class),
                (Long codeId, MutableOauth2AuthorizationCode oldCode)->{
                    if(oldCode == null) {
                        oldCode = new MutableOauth2AuthorizationCode();
                        oldCode.id = rowView.getColumn("codeId", Long.class);
                        oldCode.code = rowView.getColumn("code", String.class);
                        oldCode.creation_date = rowView.getColumn("codeCreationDate", LocalDateTime.class);
                        oldCode.effective_redirect_uri = rowView.getColumn("codeEffectiveRedirectUri", String.class);
                        oldCode.provided_redirect_uri = rowView.getColumn("codeProvidedRedirectUri", String.class);
                        oldCode.client_uid = rowView.getColumn("clientUid", Long.class);
                        oldCode.client_id = rowView.getColumn("clientId", String.class);
                        oldCode.client_token_expiration_in_seconds = rowView.getColumn("clientTokenExpirationInSeconds", Long.class);
                        oldCode.client_description = rowView.getColumn("clientDescription", String.class);
                        oldCode.client_confidential = rowView.getColumn("clientConfidential", Boolean.class);
                        oldCode.client_creation_date = rowView.getColumn("clientCreationDate", LocalDateTime.class);
                        oldCode.resource_owner_id = rowView.getColumn("resourceOwnerId", Long.class);
                        oldCode.resource_owner_username = rowView.getColumn("resourceOwnerUsername", String.class);
                        oldCode.resource_owner_auto_approve_scope = rowView.getColumn("resourceOwnerAutoApproveScope", Boolean.class);
                        oldCode.resource_owner_creation_date = rowView.getColumn("resourceOwnerCreationDate", LocalDateTime.class);
                    }
                    oldCode.scopes.add(rowView.getColumn("scope",String.class));
                    return oldCode;
                });
    }

    @Override
    public Stream<Oauth2AuthorizationCode> stream(Map<Long, MutableOauth2AuthorizationCode> container) {
        return container
                .values()
                .stream()
                .map((MutableOauth2AuthorizationCode m)->new Oauth2AuthorizationCode(m))
                .collect(Collectors.toList())
                .stream();
    }
}
