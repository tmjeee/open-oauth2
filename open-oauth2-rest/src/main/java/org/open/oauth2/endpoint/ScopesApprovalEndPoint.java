package org.open.oauth2.endpoint;

import org.open.oauth2.Utils;
import org.open.oauth2.config.OpenOAuth2ConfigurationProperties;
import org.open.oauth2.service.DbService;
import org.open.oauth2.service.Encryptor;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("oauth2")
public class ScopesApprovalEndPoint {

    public static class Res {
        public boolean ok;
        public List<String> messages = new ArrayList<>();
        public String redirect_uri;
    }

    @Autowired
    private OpenOAuth2ConfigurationProperties configurationProperties;

    @Autowired
    private DbService dbService;

    @Autowired
    private Encryptor encryptor;

    @Transactional
    @RequestMapping(value = "/approve", method = {RequestMethod.POST})
    public Res post(
            @RequestParam(value = "selected_scope", required = false) String[] selectedScopes,
            @RequestParam(value = "transaction_id", required = false) String transactionId,
            HttpServletRequest req, HttpServletResponse res)
            throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Res r = new Res();

        String encryptedContent = dbService.findByTransactionId(transactionId);
        if (StringUtils.isEmpty(encryptedContent)) {
            r.ok = false;
            r.messages.add(String.format("Invalid transaction id %s ", transactionId));
            return r;
        }

       String decryptedContent = encryptor.decript(encryptedContent);
       TransactionIdMap params = Utils.transactionContentToMap(decryptedContent);

       String newScopes = Arrays.stream(selectedScopes).collect(Collectors.joining(" "));
       params.setEntryScope(newScopes);

       String newEncryptedContent = encryptor.encrypt(Utils.toTransactionContent(params));
       dbService.insertOrUpdateTransaction(transactionId, newEncryptedContent);



       String req_uri = req.getRequestURL().toString();
       String base_uri = req_uri.substring(0, req_uri.indexOf(req.getRequestURI()));

       String redirect_uri = String.format("%s/oauth2/authorization-transaction?transaction_id=%s",
               base_uri, transactionId);

       r.ok = true;
       r.redirect_uri = redirect_uri;
       return r;
    }
}
