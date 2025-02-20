package org.fit.ssapp.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CorsConfig - Global CORS configuration for the application.
 * This class configures Cross-Origin Resource Sharing (CORS) settings to allow
 * requests from different origins. It is applied at the highest precedence.
 * ## CORS Policy:
 * - Allowed origins: Any (`*`), meaning requests from any domain are permitted.
 * - Allowed methods: All HTTP methods (`GET`, `POST`, `PUT`, `DELETE`, etc.).
 * - Allowed headers: Any header is accepted.
 * - Max age: 3600 seconds (1 hour) - caching duration for preflight requests.
 * - Allow credentials: `false` (cookies and authorization headers are not allowed).
 *
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/")
            .allowedOriginPatterns("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .maxAge(3600L)
            .allowCredentials(false);
  }
}



