package org.open.oauth2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class OpenOAuth2Application {

	private static final Logger log = LoggerFactory.getLogger(OpenOAuth2Application.class);


	public static void main(String[] args) throws IOException {
		log.info("starting OAuth2Application");
		SpringApplication.run(OpenOAuth2Application.class, args);
	}


}

