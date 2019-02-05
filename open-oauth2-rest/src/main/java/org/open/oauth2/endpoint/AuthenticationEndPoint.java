package org.open.oauth2.endpoint;

import org.open.oauth2.config.OpenOAuth2ConfigurationProperties;
import org.open.oauth2.service.DbService;
import org.open.oauth2.service.Encryptor;
import org.open.oauth2.service.Oauth2ResourceOwner;
import org.open.oauth2.service.TransactionIdMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("oauth2")
public class AuthenticationEndPoint {

    public static class Response {
        public boolean ok;
        public List<String> messages = new ArrayList<>();
        public String redirect_uri;
    }

    @Autowired
    OpenOAuth2ConfigurationProperties configurationProperties;

    @Autowired
    private Encryptor encryptor;

    @Autowired
    private DbService dbService;


    @Transactional
    @RequestMapping(value="/authentication", method= RequestMethod.POST)
    public Response authentication(
                     @RequestParam("transaction_id")String transactionId,
                     @RequestParam("username")String username,
                     @RequestParam("password")String password,
                     HttpServletRequest req,
                     HttpServletResponse res) throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, IOException {

        Response r = new Response();
        Oauth2ResourceOwner oauth2User = dbService.findResourceOwner(username);
        if (oauth2User == null) {
            r.ok = false;
            r.messages.add("No such user");
            return r;
        }
        if (!encryptor.digest(password).equals(oauth2User.password)) {
            r.ok = false;
            r.messages.add("Bad username password combination");
            return r;
        }

        String encryptedContent = dbService.findByTransactionId(transactionId);
        if (StringUtils.isEmpty(encryptedContent)) {
            r.ok = false;
            r.messages.add(String.format("Invalid transaction id %s", transactionId));
            return r;
        }
        String decriptedContent = encryptor.decript(encryptedContent);
        TransactionIdMap transactionIdMap = TransactionIdMap.toMap(decriptedContent);
        transactionIdMap.setEntryUsername(username);
        transactionIdMap.setEntryPassword(password);
        decriptedContent = transactionIdMap.toStringContent();
        encryptedContent = encryptor.encrypt(decriptedContent);
        dbService.insertOrUpdateTransaction(transactionId, encryptedContent);

        if (oauth2User.autoApproveScope) {
            r.ok = true;
            r.redirect_uri = String.format("/authorization-transaction?transaction_id=%s",transactionId);
            return r;
        } else {
            r.ok = true;
            r.redirect_uri = String.format(
                   configurationProperties.getWebconsole().getServer()+
                   configurationProperties.getWebconsole().getResourceOwnerScopesApproval(),
                   transactionId);
            return r;
        }
    }
}
