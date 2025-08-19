package org.example.carpooling.Service.Imp.RedisServiceImp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carpooling.Dto.ChatMessagePayload;
import org.example.carpooling.Service.RedisService.RedisChatPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisChatPublisherImp implements RedisChatPublisher {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    public RedisChatPublisherImp(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(ChatMessagePayload payload) {
        String topic = "chat:room:" + payload.getRoomId();
        try {
            String json = objectMapper.writeValueAsString(payload);
            stringRedisTemplate.convertAndSend(topic, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Publish chat message failed", e);
        }
    }
}
