package org.open.oauth2.endpoint;

import org.open.oauth2.Constants;
import org.open.oauth2.service.Oauth2ServerError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler
    public ResponseEntity<?> handleException(Exception e) {
        log.error(e.toString(), e);
        return new ResponseEntity<>(
                new Oauth2ServerError(Constants.ERROR_SERVER_ERROR, e.toString()),
                HttpStatus.OK);
    }
}
