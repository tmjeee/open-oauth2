package org.open.oauth2.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

public class AuthorizationHeaderInterceptor implements HandlerInterceptor  {

    private static final String AUTHORIZATION_HEADER_VALUE_BASIC_PREFIX = "basic ";

    private static final Logger log = LoggerFactory.getLogger(AuthorizationHeaderInterceptor.class);

    public interface AuthorizationHeaderAware {
        void authorizationHeader(String username, String password);
    }


    public static void main(String[] args) throws Exception {
        String s = "dGVzdC1jbGllbnQ6dGVzdC1jbGllbnQ=";
        String r1 = new String(Base64.getDecoder().decode(s));
        System.out.println("["+r1+"]");
    }


    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if ((handler instanceof HandlerMethod) &&
           (((HandlerMethod)handler).getBean() instanceof AuthorizationHeaderAware)) {

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AuthorizationHeaderAware controller = (AuthorizationHeaderAware)handlerMethod.getBean();


           String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
           if (authorizationHeader == null) {
               controller.authorizationHeader(null, null);
           } else {
               if (authorizationHeader.toLowerCase().startsWith(AUTHORIZATION_HEADER_VALUE_BASIC_PREFIX)) {
                   String encodedUsernamePassword = authorizationHeader.substring(AUTHORIZATION_HEADER_VALUE_BASIC_PREFIX.length());
                   String decodedUsernamePassword = new String(Base64.getDecoder().decode(encodedUsernamePassword));
                   int i = decodedUsernamePassword.indexOf(":");
                   if (i > 0) {
                        String username = decodedUsernamePassword.substring(0, i);
                        String password = decodedUsernamePassword.substring(i+1);
                        controller.authorizationHeader(username, password);
                   } else {
                       log.error(String.format("Bad authorization header value %s", authorizationHeader));
                   }
               } else {
                   log.error(String.format("Do not support authorization header %s", authorizationHeader));
               }
           }
        }
        return true;
    }

}
