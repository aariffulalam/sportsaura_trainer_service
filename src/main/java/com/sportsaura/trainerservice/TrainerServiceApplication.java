package com.sportsaura.trainerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point. Logins are handled by the user service; this service only checks its JWTs, so the
 * default in-memory user that Spring Boot would create is switched off.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class TrainerServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(TrainerServiceApplication.class, args);
  }
}
