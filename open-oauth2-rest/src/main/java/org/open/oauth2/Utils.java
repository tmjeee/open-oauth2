package org.open.oauth2;

import org.open.oauth2.service.AuthorizationTokenGenerator;
import org.open.oauth2.service.TransactionIdMap;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {


    public static String toHex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int a=0;a<b.length;a++) {
            char first = Character.forDigit((b[a]>>4) & 0xF, 16);
            char second = Character.forDigit(b[a] & 0xF, 16);
            sb.append(new String(new char[]{first, second}));
        }
        return sb.toString();
    }

    public static byte[] toByte(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException(String.format("Invalid HEX %s", hex));
        }
        byte[] b = new byte[hex.length()/2];
        int i = 0;
        for (int a=0; a<hex.length(); a += 2) {
            int first = (Character.digit(hex.charAt(a), 16) << 4);
            int second = Character.digit(hex.charAt(a+1), 16);
            byte _b = (byte)(first + second);
            b[i] = _b;
            i++;
        }
        return b;
    }


    public static boolean validEmail(String email) {
        if (!StringUtils.isEmpty(email)) {
            String emailRegex =
              "^[a-zA-Z0-9_+&*-]+(?:\\."+
              "[a-zA-Z0-9_+&*-]+)*@" +
              "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
              "A-Z]{2,7}$";

            Pattern pattern = Pattern.compile(emailRegex);
            return pattern.matcher(email).matches();
        }
        return false;
    }

    public static TransactionIdMap transactionContentToMap(String transactionId) {
        return TransactionIdMap.toMap(transactionId);
    }

    public static String toTransactionContent(TransactionIdMap m) {
        return m.toStringContent();
    }



    //////////////////////////////////// redirects =================================

    public static void redirectWithTransactionId(HttpServletResponse response, String format,
                                                   String transactionId) throws IOException {
        response.sendRedirect(String.format(format, transactionId));
    }



    public static String redirectAuthorizationCodeErrorUri(String redirect_uri,
                                                           String errorCode,
                                                           String state) {
        String uri = redirect_uri;
        int i = redirect_uri.indexOf("?");
        if (i > 0) {
            uri = uri + String.format("&error=%s&state=%s",errorCode,(StringUtils.isEmpty(state)?"":state));
        } else {
            uri = uri + String.format("?error=%s&state=%s",errorCode,(StringUtils.isEmpty(state)?"":state));
        }
        return uri;
    }

    public static  void redirectAuthorizationCodeError(HttpServletResponse res,
                                                       String redirect_uri,
                                                       String errorCode,
                                                       String state)
            throws IOException {
        res.sendRedirect(redirectAuthorizationCodeErrorUri(redirect_uri, errorCode, state));
    }

    public static String redirectAuthorizationCodeSuccessUri(String redirect_uri,
                                                             String code,
                                                             String state) {
        String uri = redirect_uri;
        int i = redirect_uri.indexOf("?");
        if (i > 0) {
            uri = uri + String.format("&code=%s&state=%s",code,(StringUtils.isEmpty(state)?"":state));
        } else {
            uri = uri + String.format("?code=%s&state=%s",code,(StringUtils.isEmpty(state)?"":state));
        }
        return uri;
    }

    public static void redirectAuthorizationCodeSuccess(HttpServletResponse res,
                                                      String redirect_uri,
                                                      String code,
                                                      String state) throws IOException {
        res.sendRedirect(redirectAuthorizationCodeSuccessUri(redirect_uri, code, state));
    }

    public static String redirectAuthorizationTokenErrorUri(String redirect_uri,
                                                            String code,
                                                            String state) {

        String uri = redirect_uri;
        int i = uri.indexOf("#");
        if (i > 0) {
            uri = uri + String.format("error=%s&state=%s", code, (StringUtils.isEmpty(state)?"":state));
        } else {
            uri = uri + String.format("#error=%s&state=%s", code, (StringUtils.isEmpty(state)?"":state));
        }
        return uri;
    }

    public static void redirectAuthorizationTokenError(HttpServletResponse res,
                                                       String redirect_uri,
                                                       String code,
                                                       String state) throws IOException {
        res.sendRedirect(redirectAuthorizationTokenErrorUri(redirect_uri, code, state));
    }

    public static String redirectAuthorizationTokenSuccessUri(String redirect_uri,
                                                              AuthorizationTokenGenerator.TokenGenerated token,
                                                              List<String> scopes,
                                                              String state) {
        String _scopes = scopes.stream().collect(Collectors.joining(" "));
        String uri = redirect_uri;
        int i = uri.indexOf("#");
        if (i > 0) {
            uri = uri + String.format("&access_token=%s&token_type=%s&expires_in=%s&scope=%s&state=%s",
                    token.authorization_token,token.token_type,token.expires_in, _scopes, StringUtils.isEmpty(state)?"":state);
        } else {
            uri = uri + String.format("#access_token=%s&token_type=%s&expires_in=%s&scope=%s&state=%s",
                    token.authorization_token,token.token_type,token.expires_in, _scopes, StringUtils.isEmpty(state)?"":state);
        }
        return uri;
    }

    public static void redirectAuthorizationTokenSuccess(HttpServletResponse res,
                                                         String redirect_uri,
                                                         AuthorizationTokenGenerator.TokenGenerated token,
                                                         List<String> scopes,
                                                         String state) throws IOException {
        res.sendRedirect(redirectAuthorizationTokenSuccessUri(redirect_uri, token, scopes, state));
    }

}
