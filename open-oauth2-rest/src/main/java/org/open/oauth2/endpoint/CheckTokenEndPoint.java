package org.open.oauth2.endpoint;

import org.open.oauth2.service.DbService;
import org.open.oauth2.service.Oauth2AuthorizationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("oauth2")
public class CheckTokenEndPoint {

    public static class Res {
        public boolean ok;
        public List<String> messages = new ArrayList<>();
        public Oauth2AuthorizationToken token;
    }

    @Autowired
    private DbService dbService;

    @Transactional(readOnly = true)
    @RequestMapping(value="/check-token", method = RequestMethod.GET)
    public Res get(
            @RequestParam(value = "authorization_token", required = false) String authorization_token) {
        Res r = new Res();
        if (StringUtils.isEmpty(authorization_token)) {
            r.ok=false;
            r.messages.add(String.format("Authorization token empty"));
            return r;
        }

        Oauth2AuthorizationToken token = dbService.findAuthorizationToken(authorization_token);
        if (token == null) {
            r.ok = false;
            r.messages.add(String.format("Authorization token not found"));
            return r;
        }
        if (LocalDateTime.now().isAfter(token.expire_date)) {
            r.ok = false;
            r.messages.add(String.format("Authorization token expired"));
            return r;
        }

        r.ok = true;
        r.messages.add(String.format("ok"));
        r.token = token;
        return r;
    }
}
