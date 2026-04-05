package org.fit.ssapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point for the SS2 Backend Application.
 * This class initializes and starts the Spring Boot application.
 */

@Slf4j
@SpringBootApplication(scanBasePackages = {
    "org.fit.ssapp",
    "org.c419.infra"
})
public class Ss2BackendApplication {
  /**
   * Main method to start the Spring Boot application.
   *
   * @param args Command-line arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(Ss2BackendApplication.class, args);
  }
}
