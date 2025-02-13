package org.fit.ssapp;

import org.fit.ssapp.config.ValidationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(ValidationConfig.class)
@SpringBootApplication
public class Ss2BackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(Ss2BackendApplication.class, args);
  }

}
