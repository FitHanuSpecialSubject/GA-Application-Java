package org.fit.ssapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebsocketConfiguration - Configures WebSocket messaging for real-time communication.
 * This class sets up a STOMP (Simple Text Oriented Messaging Protocol) WebSocket broker
 * for handling real-time messaging between clients and the server.
 * ## Configuration Details:
 * - Enables WebSocket message brokering with `@EnableWebSocketMessageBroker`.
 * - Configures a simple message broker (`/topic`, `/session`) for client-server messaging.
 * - Defines user-specific destinations with prefix `/session`.
 * - Sets an application destination prefix `/app` for client-to-server communication.
 * - Registers WebSocket endpoints at `/ws` with SockJS fallback support.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic", "/session");
    registry.setUserDestinationPrefix("/session");
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
  }
}
