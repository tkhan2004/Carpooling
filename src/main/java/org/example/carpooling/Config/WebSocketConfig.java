package org.example.carpooling.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint thông thường (KHÔNG dùng .withSockJS())
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // Cho phép tất cả origins
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Client nhận tin nhắn từ server từ kênh này
        config.enableSimpleBroker("/topic");

        // Client gửi tin nhắn vào /app/...
        config.setApplicationDestinationPrefixes("/app");
    }
}