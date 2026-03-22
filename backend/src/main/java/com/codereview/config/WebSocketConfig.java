package com.codereview.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration â€” STOMP over SockJS
 * Skills: RESTful API (WebSocket), Server Side, Full-Stack Web Development
 *
 * Endpoints:
 * CONNECT /ws-review â€” SockJS upgrade handshake
 * SUBSCRIBE /topic/review/{id} â€” live comment feed for a review
 * SUBSCRIBE /topic/presence â€” online-user presence updates
 * SEND /app/** â€” messages routed through @MessageMapping handlers
 * SUBSCRIBE /user/queue/notify â€” private per-user notifications
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Register the in-memory STOMP broker.
     * In production, swap SimpleBroker for a RabbitMQ/Redis relay for horizontal
     * scaling.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Clients subscribe to destinations prefixed with /topic (broadcast)
        // or /user/queue (point-to-point)
        registry.enableSimpleBroker("/topic", "/queue");

        // Messages sent by clients arrive at @MessageMapping methods via /app prefix
        registry.setApplicationDestinationPrefixes("/app");

        // Routes user-specific messages to /user/{username}/queue/...
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Register the WebSocket handshake endpoint.
     * SockJS provides automatic fallback for browsers that cannot upgrade to
     * WebSocket.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-review")
                .setAllowedOriginPatterns("*") // narrowed by SecurityConfig CORS
                .withSockJS();
    }
}
