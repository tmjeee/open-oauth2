package org.open.oauth2.endpoint;

import org.open.oauth2.Utils;
import org.open.oauth2.service.DbService;
import org.open.oauth2.service.Encryptor;
import org.open.oauth2.service.TransactionIdMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("oauth2")
public class ScopesEndPoint extends AbstractAuthorizationEndPoint {

    public static class Body {
        public boolean ok;
        public List<String> messages = new ArrayList<>();
        public String[] scopes = {};
    }

    @Autowired
    private DbService dbService;

    @Autowired
    private Encryptor encryptor;

    @Transactional(readOnly = true)
    @RequestMapping("/scope")
    public ResponseEntity<?> get(@RequestParam(value = "transactionId", required = true)String transactionId)
            throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Body r = new Body();

        String encryptedContent = dbService.findByTransactionId(transactionId);
        if (StringUtils.isEmpty(encryptedContent)) {
            r.ok=false;
            r.messages.add(String.format("Bad transaction id %s", transactionId));
            return new ResponseEntity<>(r,HttpStatus.OK);
        }
        String decryptedContent = encryptor.decript(encryptedContent);
        TransactionIdMap params = Utils.transactionContentToMap(decryptedContent);

        String scope = params.getEntryScope();

        r.ok= true;
        r.scopes = scope.split(" ");
        return new ResponseEntity<>(r, HttpStatus.OK);
    }
}
