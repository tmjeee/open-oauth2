package org.open.oauth2.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jdbi.v3.core.result.RowReducer;
import org.jdbi.v3.core.result.RowView;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Oauth2ResourceOwner {
    public final Long id;
    public final String username;
    @JsonIgnore
    public final String password;
    public final String email;
    public final boolean autoApproveScope;
    public final LocalDateTime creationDate;

    public Oauth2ResourceOwner(Long id,
                               String username,
                               String password,
                               String email,
                               boolean autoApproveScope,
                               LocalDateTime creationDate) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.autoApproveScope = autoApproveScope;
        this.creationDate = creationDate;
    }
}

class MutableOauth2User {
    public Long id;
    public String username;
    public String password;
    public String email;
    public LocalDateTime creation_date;
    public boolean auto_approve_scope;
}

class Oauth2UserRowReducer implements RowReducer<Map<Integer, MutableOauth2User>, Oauth2ResourceOwner> {

    @Override
    public Map<Integer, MutableOauth2User> container() {
        return new LinkedHashMap<>();
    }

    @Override
    public void accumulate(Map<Integer, MutableOauth2User> container, RowView rowView) {
        container.compute(
                rowView.getColumn("id", Integer.class),
                (Integer k, MutableOauth2User u)->{
                    if (u == null) {
                        u = new MutableOauth2User();
                        u.id = rowView.getColumn("id", Long.class);
                        u.username = rowView.getColumn("username", String.class);
                        u.password = rowView.getColumn("password", String.class);
                        u.email = rowView.getColumn("email", String.class);
                        u.creation_date = rowView.getColumn("creation_date", LocalDateTime.class);
                        u.auto_approve_scope = rowView.getColumn("auto_approve_scope", Boolean.class);
                    }
                    return u;
                }
        );
    }

    @Override
    public Stream<Oauth2ResourceOwner> stream(Map<Integer, MutableOauth2User> container) {
        return container
                .values()
                .stream()
                .map((MutableOauth2User u)->
                        new Oauth2ResourceOwner(
                                u.id,
                                u.username,
                                u.password,
                                u.email,
                                u.auto_approve_scope,
                                u.creation_date))
                .collect(Collectors.toList())
                .stream();
    }
}
