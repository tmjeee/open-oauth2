package org.open.oauth2.endpoint;

import org.open.oauth2.Utils;
import org.open.oauth2.service.DbService;
import org.open.oauth2.service.Encryptor;
import org.open.oauth2.service.Oauth2ResourceOwner;
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
import java.util.List;

@RestController
@RequestMapping("oauth2")
public class RegisterResourceOwnerEndPoint {

    public static class Response {
        public final boolean ok;
        public final List<String> messages;

        Response(boolean ok, List<String>messages) {
            this.ok = ok;
            this.messages = messages;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(RegisterResourceOwnerEndPoint.class);

    @Autowired
    private Encryptor encryptor;

    @Autowired
    private DbService dbService;

    @Transactional
    @RequestMapping(value="/register-resource-owner", method=RequestMethod.POST)
    public Response post(
                     @RequestParam("username")String username,
                     @RequestParam("password")String password,
                     @RequestParam("email") String email,
                     @RequestParam("confirmPassword")String confirmPassword,
                     @RequestParam(value="autoApprove", required=false)boolean autoApprove) throws NoSuchAlgorithmException {

        List<String> messages = new ArrayList<>();

        if (StringUtils.isEmpty(username)) {
            messages.add("Username is required");
        }
        if (StringUtils.isEmpty(password)) {
            messages.add("Password is required");
        }
        if (StringUtils.isEmpty(email) || (!Utils.validEmail(email))) {
            messages.add("Email is invalid");
        }
        if (StringUtils.isEmpty(confirmPassword)) {
            messages.add("Confirmed` password is required");
        }
        if (!StringUtils.isEmpty(password) && !StringUtils.isEmpty(confirmPassword) && !password.equals(confirmPassword)) {
            messages.add("Password and confirmed password do not match");
        }
        Oauth2ResourceOwner oauth2ResourceOwner = dbService.findResourceOwner(username);
        if (oauth2ResourceOwner != null) {
            messages.add(String.format("Resource owner with username %s is already taken", username));
        }

        if (!messages.isEmpty()) {
            return new Response(false, messages);
        }

        String digestedPassword = encryptor.digest(password);

        dbService.insertResourceOwner(new Oauth2ResourceOwner(
          -1L, username, digestedPassword, email, autoApprove, LocalDateTime.now()));

        messages.add(String.format("Resource owner %s successfully created", username));
        return new Response(true, messages);

    }

}
