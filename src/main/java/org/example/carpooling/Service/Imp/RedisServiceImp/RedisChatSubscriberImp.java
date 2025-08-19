package org.example.carpooling.Service.Imp.RedisServiceImp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carpooling.Dto.ChatMessagePayload;
import org.example.carpooling.Service.RedisService.RedisChatSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisChatSubscriberImp implements RedisChatSubscriber {

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;



    @Autowired
    public RedisChatSubscriberImp(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;

    }

    @Override
    public void onMessage(String message, String channel) {
        try {
            ChatMessagePayload payload = objectMapper.readValue(message, ChatMessagePayload.class);
            // Đẩy tới tất cả client subscribe /topic/chat/{roomId}
            String destination = "/topic/chat/" + payload.getRoomId();
            messagingTemplate.convertAndSend(destination, payload);
            System.out.println("Redis nhận 1 tin nhắn: " + payload.getContent() + " room=" + payload.getRoomId());
        } catch (Exception e) {
            // log lỗi để không kill thread listener
            e.printStackTrace();
        }
    }
}
