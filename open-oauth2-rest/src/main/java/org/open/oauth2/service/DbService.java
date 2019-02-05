package org.open.oauth2.service;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DbService {

    private static final String KEY_REDIRECT_URI = "redirect_uri";


    @Autowired
    private Jdbi jdbi;

    public List<String> findAuthorizationRedirectURIs(String clientId) {
        return jdbi.inTransaction((Handle h)->
            h.createQuery("SELECT REDIRECT_URI FROM TBL_CLIENT_REDIRECT_URI AS C_URI " +
                    "INNER JOIN TBL_CLIENT AS C ON C.ID = C_URI.CLIENT_ID " +
                    "WHERE C.CLIENT_ID = :clientId")
                    .bind("clientId", clientId)
                    .mapTo(String.class)
                    .list()
        );
    }

    public Oauth2Client findClient(String clientId) {
        return jdbi.inTransaction((Handle h)->
            h.createQuery(
              "SELECT client.ID as id, " +
                  "client.CLIENT_ID as client_id, " +
                  "client.CLIENT_SECRET as client_secret, " +
                  "client.EMAIL as email, "+
                  "client.DESCRIPTION as description, " +
                  "client.CONFIDENTIAL as confidential, " +
                  "client.TOKEN_EXPIRATION_IN_SECONDS as token_expiration_in_seconds, "+
                  "client.CREATION_DATE as creation_date, "+
                  "grantType.GRANT_TYPE as grant_type, " +
                  "redirectUri.REDIRECT_URI as redirect_uri, " +
                  "scope.SCOPE as scope "+
                  "FROM TBL_CLIENT as client " +
                  "INNER JOIN TBL_CLIENT_GRANT_TYPE as grantType ON grantType.CLIENT_ID = client.ID "+
                  "INNER JOIN TBL_CLIENT_REDIRECT_URI as redirectUri ON redirectUri.CLIENT_ID = client.ID "+
                  "INNER JOIN TBL_CLIENT_SCOPE as scope ON scope.CLIENT_ID = client.ID "+
                  "WHERE client.CLIENT_ID = :clientId"
            )
            .bind("clientId", clientId)
            .reduceRows(new Oauth2ClientRowReducer())
            .findFirst()
            .orElse(null)
        );
    }

    public Oauth2ResourceOwner findResourceOwner(String username) {
        return jdbi.inTransaction((Handle h)->
            h.createQuery(
                    "SELECT ID as id, " +
                        "USERNAME as username, " +
                        "PASSWORD as password, " +
                        "EMAIL as email, " +
                        "AUTO_APPROVE_SCOPE as auto_approve_scope, " +
                        "CREATION_DATE as creation_date " +
                        "FROM TBL_RESOURCE_OWNER "+
                        "WHERE USERNAME = :username"
            )
            .bind("username", username)
            .reduceRows(new Oauth2UserRowReducer())
            .findFirst()
            .orElse(null)
        );
    }


    public void insertAuthorizationCode(
            long clientId,
            long resourceOwnerId,
            String effective_redirect_uri,  // worked out by oauth2 server
            String redirect_uri,            // provided by client might be empty
            List<String> scopes,
            String authorizationCode,
            LocalDateTime creationDate) {
        jdbi.useTransaction((Handle h)->{
            Long codeId = h.createUpdate(
                    "INSERT INTO TBL_CLIENT_AUTHORIZATION_CODE ( "+
                       "CLIENT_ID, RESOURCE_OWNER_ID, CODE, CREATION_DATE, " +
                       "EFFECTIVE_REDIRECT_URI, PROVIDED_REDIRECT_URI ) VALUES ( " +
                       ":client_id, :resource_owner_id, :code, :creation_date, :effective_redirect_uri, :provided_redirect_uri)"
            )
            .bind("client_id", clientId)
            .bind("resource_owner_id", resourceOwnerId)
            .bind("code", authorizationCode)
            .bind("creation_date", creationDate)
            .bind("effective_redirect_uri", effective_redirect_uri)
            .bind("provided_redirect_uri", redirect_uri)
            .executeAndReturnGeneratedKeys("ID")
            .mapTo(Long.class)
            .findOnly();

            scopes.forEach((String _scope)->{
                h.createUpdate("INSERT INTO TBL_CLIENT_AUTHORIZATION_CODE_SCOPE (CODE_ID, SCOPE) VALUES (:code_id, :scope)")
                .bind("code_id", codeId)
                .bind("scope", _scope)
                .execute();
            });

            h.commit();
        });
    }

    public void insertAuthorizationToken(Long clientId, Long resourceOwnerId, List<String> scopes,
                                         String authorizationToken, String refreshToken,
                                         LocalDateTime expirationDate, LocalDateTime creationDate) {
        jdbi.useTransaction((Handle h)->{
            Long tokenId =  h.createUpdate(
                    "INSERT INTO TBL_CLIENT_AUTHORIZATION_TOKEN ( " +
                       "CLIENT_ID, RESOURCE_OWNER_ID, AUTHORIZATION_TOKEN, REFRESH_TOKEN, EXPIRE_DATE, CREATION_DATE) VALUES (" +
                       ":client_id, :resource_owner_id, :authorization_token, :refresh_token, :expire_date, :creation_date) "
            )
            .bind("client_id", clientId)
            .bind("resource_owner_id", resourceOwnerId)
            .bind("authorization_token", authorizationToken)
            .bind("refresh_token", refreshToken)
            .bind("expire_date", expirationDate)
            .bind("creation_date", creationDate)
            .executeAndReturnGeneratedKeys("ID")
            .mapTo(Long.class)
            .findOnly();


            scopes.forEach((String _scope)->{
                h.createUpdate(
                        "INSERT INTO TBL_CLIENT_AUTHORIZATION_TOKEN_SCOPE (TOKEN_ID, SCOPE) VALUES (:token_id, :scope) "
                )
                .bind("token_id", tokenId)
                .bind("scope", _scope)
                .execute();
            });

            h.commit();
        });
    }

    public void insertResourceOwner(Oauth2ResourceOwner oauth2ResourceOwner) {
        jdbi.useTransaction((Handle h)->{
            h.createUpdate(
                    "INSERT INTO TBL_RESOURCE_OWNER (USERNAME, EMAIL, PASSWORD, AUTO_APPROVE_SCOPE, CREATION_DATE) VALUES (:username, :email, :password, :auto_approve_scope, :creation_date)")
                    .bind("username", oauth2ResourceOwner.username)
                    .bind("password", oauth2ResourceOwner.password)
                    .bind("email", oauth2ResourceOwner.email)
                    .bind("auto_approve_scope", oauth2ResourceOwner.autoApproveScope)
                    .bind("creation_date", oauth2ResourceOwner.creationDate)
                    .execute();
            h.commit();
        });
    }

    public void insertClient(Oauth2Client oauth2Client, String digestedClientSecret) {
        jdbi.useTransaction((Handle h)->{
            long id = h.createUpdate(
                    "INSERT INTO TBL_CLIENT (CLIENT_ID, EMAIL, CLIENT_SECRET, " +
                    "TOKEN_EXPIRATION_IN_SECONDS, DESCRIPTION, CONFIDENTIAL, " +
                    "CREATION_DATE) VALUES (:client_id, :email, :client_secret, " +
                    ":token_expiration_in_seconds, :description, :confidential, " +
                    ":creation_date)")
                    .bind("client_id", oauth2Client.client_id)
                    .bind("email", oauth2Client.email)
                    .bind("client_secret", digestedClientSecret)
                    .bind("token_expiration_in_seconds", oauth2Client.token_expiration_in_seconds)
                    .bind("description", oauth2Client.description)
                    .bind("confidential", oauth2Client.confidential)
                    .bind("creation_date", oauth2Client.creationDate)
                    .executeAndReturnGeneratedKeys("ID")
                    .mapTo(Long.class)
                    .findOnly();

            for (String grant_type : oauth2Client.grant_types) {
                h.createUpdate(
                        "INSERT INTO TBL_CLIENT_GRANT_TYPE (CLIENT_ID, GRANT_TYPE) VALUES (:client_id, :grant_type);")
                        .bind("client_id", id)
                        .bind("grant_type", grant_type)
                        .execute();
            }

            for (String scope : oauth2Client.scopes) {
                h.createUpdate(
                        "INSERT INTO TBL_CLIENT_SCOPE (CLIENT_ID, SCOPE) VALUES (:client_id, :scope)")
                        .bind("client_id", id)
                        .bind("scope", scope)
                        .execute();
            }

            for (String redirect_uri: oauth2Client.redirect_uris) {
                h.createUpdate(
                        "INSERT INTO TBL_CLIENT_REDIRECT_URI (CLIENT_ID, REDIRECT_URI) VALUES (:client_id, :redirect_uri)")
                        .bind("client_id", id)
                        .bind("redirect_uri", redirect_uri)
                        .execute();
            }

            h.commit();
        });
    }


    public Oauth2AuthorizationCode findAuthorizationCode(String code) {
        return jdbi.inTransaction((Handle h)-> {
            Oauth2AuthorizationCode c =
              h.createQuery(
                "SELECT code.ID as codeId, " +
                     "code.ID as codeId, " +
                     "code.CODE as code, " +
                     "code.CREATION_DATE as codeCreationDate, " +
                     "code.EFFECTIVE_REDIRECT_URI as codeEffectiveRedirectUri, " +
                     "code.PROVIDED_REDIRECT_URI as codeProvidedRedirectUri, " +
                     "client.ID as clientUid, " +
                     "client.CLIENT_ID as clientId, " +
                     "client.TOKEN_EXPIRATION_IN_SECONDS as clientTokenExpirationInSeconds, " +
                     "client.DESCRIPTION as clientDescription, " +
                     "client.CONFIDENTIAL as clientConfidential, " +
                     "client.CREATION_DATE as clientCreationDate, " +
                     "resourceOwner.ID as resourceOwnerId, " +
                     "resourceOwner.USERNAME as resourceOwnerUsername, " +
                     "resourceOwner.AUTO_APPROVE_SCOPE as resourceOwnerAutoApproveScope, " +
                     "resourceOwner.CREATION_DATE resourceOwnerCreationDate, " +
                     "scope.SCOPE as scope " +
                  "FROM TBL_CLIENT_AUTHORIZATION_CODE AS code " +
                     "INNER JOIN TBL_CLIENT AS client ON client.ID = code.CLIENT_ID " +
                     "INNER JOIN TBL_RESOURCE_OWNER AS resourceOwner ON resourceOwner.ID = code.RESOURCE_OWNER_ID " +
                     "INNER JOIN TBL_CLIENT_AUTHORIZATION_CODE_SCOPE AS scope ON scope.CODE_ID = code.ID " +
                     "WHERE code.CODE = :code")
              .bind("code", code)
              .reduceRows(new Oauth2AuthorizationCodeRowReducer())
              .findFirst().
              orElse(null);
            return c;
          }
        );
    }

    public void deleteAuthorizationCode(Long id) {
        jdbi.useTransaction((Handle h)-> {
            h.createUpdate("DELETE FROM TBL_CLIENT_AUTHORIZATION_CODE_SCOPE WHERE CODE_ID = :codeId")
                    .bind("codeId", id)
                    .execute();
            h.createUpdate("DELETE FROM TBL_CLIENT_AUTHORIZATION_CODE WHERE ID = :codeId")
                    .bind("codeId", id)
                    .execute();
        });
    }

    public Oauth2AuthorizationToken findAuthorizationTokenByRefreshToken(String refresh_token) {
        return jdbi.inTransaction((Handle h)-> {
            Oauth2AuthorizationToken t =
                h.createQuery(
                    "SELECT " +
                            "token.ID as tokenId, " +
                            "token.AUTHORIZATION_TOKEN as authorizationToken, " +
                            "token.REFRESH_TOKEN as refreshToken, " +
                            "token.EXPIRE_DATE as expireDate, " +
                            "token.CREATION_DATE as creationDate, " +
                            "client.ID as clientUid, " +
                            "client.CLIENT_ID as clientId, " +
                            "client.TOKEN_EXPIRATION_IN_SECONDS as clientTokenExpirationInSeconds, " +
                            "client.DESCRIPTION as clientDescription, " +
                            "client.CONFIDENTIAL as clientConfidential, " +
                            "client.CREATION_DATE as clientCreationDate, " +
                            "resourceOwner.ID as resourceOwnerId, " +
                            "resourceOwner.USERNAME as resourceOwnerUsername, " +
                            "resourceOwner.AUTO_APPROVE_SCOPE as resourceOwnerAutoApproveScope, " +
                            "resourceOwner.CREATION_DATE resourceOwnerCreationDate, " +
                            "scope.SCOPE as scope " +
                    "FROM TBL_CLIENT_AUTHORIZATION_TOKEN as token " +
                            "INNER JOIN TBL_RESOURCE_OWNER AS resourceOwner ON resourceOwner.ID = token.RESOURCE_OWNER_ID " +
                            "INNER JOIN TBL_CLIENT_AUTHORIZATION_TOKEN_SCOPE AS scope ON scope.TOKEN_ID = token.ID " +
                            "INNER JOIN TBL_CLIENT AS client ON client.ID = token.CLIENT_ID "+
                            "WHERE token.REFRESH_TOKEN = :refresh_token")
                .bind("refresh_token", refresh_token)
                .reduceRows(new Oauth2AuthorizationTokenRowReducer())
                .findFirst()
                .orElse(null);
            return t;
        });
    }

    public Oauth2AuthorizationToken findAuthorizationToken(String authorization_token) {
        return jdbi.inTransaction((Handle h)-> {
            Oauth2AuthorizationToken t =
                h.createQuery(
                  "SELECT " +
                        "token.ID as tokenId, " +
                        "token.AUTHORIZATION_TOKEN as authorizationToken, " +
                        "token.REFRESH_TOKEN as refreshToken, " +
                        "token.EXPIRE_DATE as expireDate, " +
                        "token.CREATION_DATE as creationDate, " +
                        "client.ID as clientUid, " +
                        "client.CLIENT_ID as clientId, " +
                        "client.TOKEN_EXPIRATION_IN_SECONDS as clientTokenExpirationInSeconds, " +
                        "client.DESCRIPTION as clientDescription, " +
                        "client.CONFIDENTIAL as clientConfidential, " +
                        "client.CREATION_DATE as clientCreationDate, " +
                        "resourceOwner.ID as resourceOwnerId, " +
                        "resourceOwner.USERNAME as resourceOwnerUsername, " +
                        "resourceOwner.AUTO_APPROVE_SCOPE as resourceOwnerAutoApproveScope, " +
                        "resourceOwner.CREATION_DATE resourceOwnerCreationDate, " +
                        "scope.SCOPE as scope " +
                    "FROM TBL_CLIENT_AUTHORIZATION_TOKEN as token "+
                        "LEFT JOIN TBL_RESOURCE_OWNER AS resourceOwner ON resourceOwner.ID = token.RESOURCE_OWNER_ID " +
                        "INNER JOIN TBL_CLIENT_AUTHORIZATION_TOKEN_SCOPE AS scope ON scope.TOKEN_ID = token.ID " +
                        "INNER JOIN TBL_CLIENT AS client ON client.ID = token.CLIENT_ID "+
                        "WHERE token.AUTHORIZATION_TOKEN = :authorization_token")
            .bind("authorization_token", authorization_token)
            .reduceRows(new Oauth2AuthorizationTokenRowReducer())
            .findFirst()
            .orElse(null);
            return t;
        });
    }

    public void deleteAuthorizationToken(Long id) {
        jdbi.useTransaction((Handle h)->{
            h.createUpdate(
                    "DELETE FROM TBL_CLIENT_AUTHORIZATION_TOKEN_SCOPE WHERE TOKEN_ID = :id"
            ).bind("id", id)
            .execute();

            h.createUpdate(
                    "DELETE FROM TBL_CLIENT_AUTHORIZATION_TOKEN WHERE ID=:id"
            ).bind("id", id)
            .execute();
        });
    }

    public void deleteAuthorizationTokenByClientAndResourceOwner(Long clientId, Long resourceOwnerId) {
        jdbi.useTransaction((Handle h)->{
            if (resourceOwnerId != null) {
                h.createUpdate(
                  "DELETE FROM TBL_CLIENT_AUTHORIZATION_TOKEN_SCOPE WHERE TOKEN_ID IN (" +
                    "SELECT ID FROM TBL_CLIENT_AUTHORIZATION_TOKEN WHERE CLIENT_ID=:client_id AND RESOURCE_OWNER_ID=:resource_owner_id)"
                ).bind("client_id", clientId)
                  .bind("resource_owner_id", resourceOwnerId)
                  .execute();

                h.createUpdate(
                  "DELETE FROM TBL_CLIENT_AUTHORIZATION_TOKEN WHERE CLIENT_ID=:client_id AND RESOURCE_OWNER_ID=:resource_owner_id"
                ).bind("client_id", clientId)
                  .bind("resource_owner_id", resourceOwnerId)
                  .execute();
            } else {
                h.createUpdate(
                  "DELETE FROM TBL_CLIENT_AUTHORIZATION_TOKEN_SCOPE WHERE TOKEN_ID IN (" +
                    "SELECT ID FROM TBL_CLIENT_AUTHORIZATION_TOKEN WHERE CLIENT_ID=:client_id AND RESOURCE_OWNER_ID IS NULL)"
                ).bind("client_id", clientId)
                .execute();

                h.createUpdate(
                  "DELETE FROM TBL_CLIENT_AUTHORIZATION_TOKEN WHERE CLIENT_ID=:client_id AND RESOURCE_OWNER_ID IS NULL"
                ).bind("client_id", clientId)
                .execute();
            }
        });
    }


    public void deleteAuthorizationCodeByClientAndResourceOwner(Long clientId, Long resourceOwnerId) {
        jdbi.useTransaction((Handle h)->{
            h.createUpdate(
                    "DELETE FROM TBL_CLIENT_AUTHORIZATION_CODE_SCOPE WHERE CODE_ID IN (" +
                            "SELECT ID FROM TBL_CLIENT_AUTHORIZATION_CODE WHERE CLIENT_ID=:client_id AND RESOURCE_OWNER_ID=:resource_owner_id)")
            .bind("client_id", clientId)
            .bind("resource_owner_id", resourceOwnerId)
            .execute();

            h.createUpdate(
                    "DELETE FROM TBL_CLIENT_AUTHORIZATION_CODE WHERE CLIENT_ID=:client_id AND RESOURCE_OWNER_ID=:resource_owner_id")
            .bind("client_id", clientId)
            .bind("resource_owner_id", resourceOwnerId)
            .execute();
        });
    }

    public void insertOrUpdateTransaction(String transactionId, String encryptedContent) {
        jdbi.useTransaction((Handle h)->{
            String transaction_id = h.createQuery("SELECT TRANSACTION_ID FROM TBL_TRANSACTION WHERE TRANSACTION_ID = :transaction_id")
                    .bind("transaction_id", transactionId)
                    .mapTo(String.class)
                    .findFirst()
                    .orElse(null);
            if (StringUtils.isEmpty(transaction_id)) {
                // insert
                h.createUpdate(
                        "INSERT INTO TBL_TRANSACTION (TRANSACTION_ID, ENCRYPTED_CONTENT) VALUES (:transaction_id, :encrypted_content)")
                .bind("transaction_id", transactionId)
                .bind("encrypted_content", encryptedContent)
                .execute();
            } else {
                // update
                h.createUpdate(
                       "UPDATE TBL_TRANSACTION SET ENCRYPTED_CONTENT = :encrypted_content WHERE TRANSACTION_ID = :transaction_id")
                .bind("encrypted_content", encryptedContent)
                .bind("transaction_id", transaction_id)
                .execute();
            }
        });
    }

    public String findByTransactionId(String transactionId) {
        return jdbi.inTransaction((Handle h)->
           h.createQuery("SELECT ENCRYPTED_CONTENT FROM TBL_TRANSACTION WHERE TRANSACTION_ID = :transaction_id")
           .bind("transaction_id", transactionId)
           .mapTo(String.class)
           .findFirst()
           .orElse(null)
        );
    }

    public void deleteTransaction(String transactionId) {
        jdbi.useTransaction((Handle h)->{
            h.createUpdate(
                    "DELETE FROM TBL_TRANSACTION WHERE TRANSACTION_ID = :transaction_id")
            .bind("transaction_id", transactionId)
            .execute();
        });
    }
}
