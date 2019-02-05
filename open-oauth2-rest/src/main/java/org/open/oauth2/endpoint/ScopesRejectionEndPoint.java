package org.open.oauth2.endpoint;


import org.open.oauth2.Constants;
import org.open.oauth2.Utils;
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
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("oauth2")
public class ScopesRejectionEndPoint {

    public static class Res {
        public boolean ok;
        public List<String> messages = new ArrayList<>();
        public String redirect_uri;
    }

    @Autowired
    private DbService dbService;

    @Autowired
    private Encryptor encryptor;

    @Transactional
    @RequestMapping(value = "/reject", method = {RequestMethod.POST})
    public Res post(
            @RequestParam(value = "transaction_id", required = false) String transactionId,
            HttpServletRequest req, HttpServletResponse res)
            throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {

        Res r = new Res();

        String encryptedContent = dbService.findByTransactionId(transactionId);
        if (StringUtils.isEmpty(encryptedContent)) {
            r.ok = false;
            r.messages.add(String.format(" Invalid transaction id %s ", transactionId));
            return r;
        }
        String decriptedContent = encryptor.decript(encryptedContent);
        TransactionIdMap params = Utils.transactionContentToMap(decriptedContent);

        dbService.deleteTransaction(transactionId);

        r.ok = true;
        r.redirect_uri = Utils.redirectAuthorizationCodeErrorUri(
                params.getEntryEffectiveRedirectUri(),
                Constants.ERROR_ACCESS_DENIED,
                params.getEntryState());

        return r;
    }
}
