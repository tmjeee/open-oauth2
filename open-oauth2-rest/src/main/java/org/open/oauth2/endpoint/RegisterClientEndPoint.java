package org.open.oauth2.endpoint;

import org.open.oauth2.Utils;
import org.open.oauth2.service.DbService;
import org.open.oauth2.service.Encryptor;
import org.open.oauth2.service.Oauth2Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("oauth2")
public class RegisterClientEndPoint {

    public static class Response {
        public final boolean ok;
        public final List<String> messages;

        Response(boolean ok, List<String>messages) {
            this.ok = ok;
            this.messages = messages;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(RegisterClientEndPoint.class);

    @Autowired
    private Encryptor encryptor;

    @Autowired
    private DbService dbService;

    @Transactional
    @RequestMapping(value = "/register-client", method = RequestMethod.POST)
    public Response post(
                     @RequestParam(value="client_id", required=false)String client_id,
                     @RequestParam(value="client_secret", required=false)String client_secret,
                     @RequestParam(value="description", required=false)String description,
                     @RequestParam(value="email", required=false)String email,
                     @RequestParam(value="expiration", required=false)String expiration,
                     @RequestParam(value="confidential", required=false)boolean confidential,
                     @RequestParam(value="grant_type", required=false)String[] grantTypes,
                     @RequestParam(value="scope", required=false)String[] scopes,
                     @RequestParam(value="redirect_uri", required=false)String[] redirect_uris)
            throws NoSuchAlgorithmException {
        // validations

        List<String> messages = new ArrayList<>();

        if (StringUtils.isEmpty("client_id")) {
            messages.add("Empty client id");
        }
        if (StringUtils.isEmpty(client_secret)) {
            messages.add(String.format("Empty client secret"));
        }
        if (StringUtils.isEmpty(description)) {
            messages.add(String.format("Empty description"));
        }
        if (grantTypes == null || grantTypes.length == 0) {
            messages.add(String.format("At least one Grant Type is needed"));
        }
        if (scopes == null || scopes.length == 0) {
            messages.add(String.format("At least one scope is needed"));
        }
        if (StringUtils.isEmpty(email) || (!Utils.validEmail(email))) {
            messages.add(String.format("Email is not valid"));
        }
        if (StringUtils.isEmpty("expiration")) {
            messages.add(String.format("Expiration cannot be empty"));
        } else {
            try {
                Integer.parseInt(expiration);
            }catch(NumberFormatException e) {
               messages.add(String.format("Expiration needs to be in numbers (seconds)"));
            }
        }
        if (scopes != null && scopes.length > 0) {
            int a[] = {0};
            Arrays.stream(scopes).forEach((String scope)->{
                if (StringUtils.isEmpty(scope)) {
                    messages.add(String.format("Scope #%s must not be empty", a[0]+1));
                }
                if (scope != null && scope.trim().indexOf(" ")> 0) {
                    messages.add(String.format("scope #%s must not contains space(s)",a[0]+1));
                }
                a[0]++;
            });
        }
        if (redirect_uris != null && redirect_uris.length > 0) {
            int[] a = {0};
            Arrays.stream(redirect_uris).forEach((String redirect_uri)->{
                if (StringUtils.isEmpty(redirect_uri)) {
                    messages.add(String.format("Redirect uri #%s must not be empty", a[0]+1));
                }
                if (redirect_uri != null && (!(redirect_uri.trim().toLowerCase().startsWith("http://") || redirect_uri.trim().toLowerCase().startsWith("https://")))) {
                    System.out.println((redirect_uri != null));
                    System.out.println((!redirect_uri.trim().toLowerCase().startsWith("https://")));
                    System.out.println((!redirect_uri.trim().toLowerCase().startsWith("http://")));
                    System.out.println(redirect_uri != null &&
                            (
                             (!redirect_uri.trim().toLowerCase().startsWith("http://")) ||
                             (!redirect_uri.trim().toLowerCase().startsWith("https://"))
                            )
                    );
                    messages.add(String.format("Redirect uri #%s (%s) scheme must be either http:// or https://", a[0]+1, redirect_uri));
                }
                if (redirect_uri != null && redirect_uri.indexOf("#") > 0) {
                    messages.add(String.format("Redirect uri #%s must not contains fragment(s)", a[0]+1));
                }
                a[0]++;
            });
        }
        Oauth2Client oauth2Client = dbService.findClient(client_id);
        if (oauth2Client != null) {
            messages.add(String.format("Client with id %s already exists", client_id));
        }

        if (!messages.isEmpty()) {
            return new Response(false, messages);
        }

        dbService.insertClient(new Oauth2Client(
                -1L, client_id, client_secret, email,
                description, confidential,
                Integer.parseInt(expiration),
                LocalDateTime.now(),
                List.of(grantTypes),
                List.of(redirect_uris), List.of(scopes)
        ), encryptor.digest(client_secret));
        messages.add(String.format("Client %s successfully added", client_id));
        return new Response(true, messages);
    }
}
