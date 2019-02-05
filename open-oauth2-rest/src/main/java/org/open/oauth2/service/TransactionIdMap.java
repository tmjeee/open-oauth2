package org.open.oauth2.service;

import org.springframework.util.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TransactionIdMap {
    // "client_id=%s&response_type=%s&scope=%s&state=%s&effective_redirect_uri=%s&redirect_uri=%s"
    // _transactionId = _transactionId + "&username="+username+"&password="+password;
    public static final String ENTRY_CLIENT_ID = "client_id";
    public static final String ENTRY_RESPONSE_TYPE = "response_type";
    public static final String ENTRY_SCOPE = "scope";
    public static final String ENTRY_STATE = "state";
    public static final String ENTRY_EFFECTIVE_REDIRECT_URI = "effective_redirect_uri";
    public static final String ENTRY_REDIRECT_URI = "redirect_uri";
    public static final String ENTRY_USERNAME = "username";
    public static final String ENTRY_PASSWORD = "password";

    private Map<String,String> m = new LinkedHashMap<>();

    public String getEntryScope() {
        return m.get(ENTRY_SCOPE);
    }
    public void setEntryScope(String scope) {
        m.put(ENTRY_SCOPE, scope);
    }

    public String getEntryClientId() {
        return m.get(ENTRY_CLIENT_ID);
    }
    public void setEntryClientId(String clientId) {
        m.put(ENTRY_CLIENT_ID, clientId);
    }

    public String getEntryResponseType() {
        return m.get(ENTRY_RESPONSE_TYPE);
    }
    public void setEntryResponseType(String responseType) {
        m.put(ENTRY_RESPONSE_TYPE, responseType);
    }

    public String getEntryState() {
        return m.get(ENTRY_STATE);
    }
    public void setEntryState(String state) {
        m.put(ENTRY_STATE, state);
    }

    public String getEntryEffectiveRedirectUri() {
        return m.get(ENTRY_EFFECTIVE_REDIRECT_URI);
    }
    public void setEntryEffectiveRedirectUri(String effectiveRedirectUri) {
        m.put(ENTRY_EFFECTIVE_REDIRECT_URI, effectiveRedirectUri);
    }

    public String getEntryRedirectUri() {
        return m.get(ENTRY_REDIRECT_URI);
    }
    public void setEntryRedirectUri(String redirectUri) {
        m.put(ENTRY_REDIRECT_URI, redirectUri);
    }

    public String getEntryUsername() {
        return m.get(ENTRY_USERNAME);
    }
    public void setEntryUsername(String username) {
        m.put(ENTRY_USERNAME, username);
    }

    public String getEntryPassword() {
        return m.get(ENTRY_PASSWORD);
    }
    public void setEntryPassword(String password) {
        m.put(ENTRY_PASSWORD, password);
    }

    public String toStringContent() {
        // "client_id=%s&response_type=%s&scope=%s&state=%s&effective_redirect_uri=%s&redirect_uri=%s"
        // _transactionId = _transactionId + "&username="+username+"&password="+password;
        return String.format(
                "client_id=%s&response_type=%s&scope=%s&state=%s&effective_redirect_uri=%s&redirect_uri=%s&username=%s&password=%s",
                StringUtils.isEmpty(getEntryClientId())?"":getEntryClientId(),
                StringUtils.isEmpty(getEntryResponseType())?"":getEntryResponseType(),
                StringUtils.isEmpty(getEntryScope())?"":getEntryScope(),
                StringUtils.isEmpty(getEntryState())?"":getEntryState(),
                StringUtils.isEmpty(getEntryEffectiveRedirectUri())?"":getEntryEffectiveRedirectUri(),
                StringUtils.isEmpty(getEntryRedirectUri())?"":getEntryRedirectUri(),
                StringUtils.isEmpty(getEntryUsername())?"":getEntryUsername(),
                StringUtils.isEmpty(getEntryPassword())?"":getEntryPassword());
    }

    public static TransactionIdMap toMap(String decryptedContent) {
        System.out.println("************* decrypted="+decryptedContent);
        TransactionIdMap m = new TransactionIdMap();
        String[] paramNameValues = decryptedContent.split("&");
        for (int a=0; a<paramNameValues.length; a++) {
            System.out.println("**************************** "+paramNameValues[a]);
            String[] paramNameValue = paramNameValues[a].split("=");
            if (paramNameValue.length >= 1) {
                String paramName = paramNameValue[0];
                String paramValue = ((paramNameValue.length == 2)?paramNameValue[1]:"");

                m.m.put(paramName, paramValue);
            }
        }
        return m;
    }

    public static TransactionIdMap toMap(Encryptor encryptor, String encryptedContent)
            throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
                   IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
       return toMap(encryptor.decript(encryptedContent));
    }


    public static void main(String[] args) throws Exception {
        String[] arr = "state=".split("=");
        System.out.println(arr.length);
    }
}
