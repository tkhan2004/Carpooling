package org.example.carpooling.Service.RedisService;

import org.example.carpooling.Dto.ChatMessagePayload;

public interface RedisChatPublisher {
    public void publish(ChatMessagePayload payload);
}
