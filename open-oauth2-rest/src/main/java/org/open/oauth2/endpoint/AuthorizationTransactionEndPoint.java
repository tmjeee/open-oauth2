package org.open.oauth2.endpoint;

import org.open.oauth2.Utils;
import org.open.oauth2.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@RestController
@RequestMapping("oauth2")
public class AuthorizationTransactionEndPoint extends AbstractAuthorizationEndPoint {

    @Autowired
    private DbService dbService;

    @Autowired
    private Encryptor encryptor;

    @Autowired
    private AuthorizationTokenGenerator authorizationTokenGenerator;

    @Autowired
    private AuthorizationCodeGenerator authorizationCodeGenerator;

    @Transactional
    @RequestMapping("/authorization-transaction")
    public ResponseEntity<?> getPost(@RequestParam(value = "transaction_id", required = true)String transaction_id,
                                  HttpServletResponse response)
            throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {

        String encryptedContent = dbService.findByTransactionId(transaction_id);
        String decryptedContent =  encryptor.decript(encryptedContent);

        TransactionIdMap m = Utils.transactionContentToMap(decryptedContent);

        String client_id = m.getEntryClientId();
        String response_type = m.getEntryResponseType();
        String scope = m.getEntryScope();
        String state = m.getEntryState();
        String effective_redirect_uri = m.getEntryEffectiveRedirectUri();
        String redirect_uri = m.getEntryRedirectUri();
        String username = m.getEntryUsername();
        String password = m.getEntryPassword();

        Oauth2Client oauth2Client = dbService.findClient(client_id);
        Oauth2ResourceOwner oauth2User = dbService.findResourceOwner(username);

        dbService.deleteTransaction(transaction_id);

        redirectCodeOrTokenToClient(
                response,
                response_type,
                effective_redirect_uri,
                redirect_uri,
                state,
                Arrays.asList(scope.split(" ")),
                oauth2Client,
                oauth2User
        );

        return null;
    }
}
