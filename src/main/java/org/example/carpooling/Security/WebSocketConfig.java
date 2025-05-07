package org.example.carpooling.Security;

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
        // Cho phép kết nối từ frontend (mobile/web) qua SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Cho phép mọi origin trong môi trường dev
                .withSockJS();
            
        // Thêm endpoint không sử dụng SockJS cho Postman
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Client nhận tin nhắn từ server từ kênh này
        config.enableSimpleBroker("/topic");

        // Client gửi tin nhắn vào /app/...
        config.setApplicationDestinationPrefixes("/app");
    }
}
