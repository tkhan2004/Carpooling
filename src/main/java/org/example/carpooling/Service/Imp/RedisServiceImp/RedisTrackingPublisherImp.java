package org.example.carpooling.Service.Imp.RedisServiceImp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carpooling.Dto.TrackingPayloadDTO;
import org.example.carpooling.Service.RedisService.RedisTrackingPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTrackingPublisherImp implements RedisTrackingPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisTrackingPublisherImp(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(TrackingPayloadDTO tracking) {
        if (tracking.getRideId() == null) {
            throw new IllegalArgumentException("rideId không được null");
        }

        String topic = "tracking:ride:" + tracking.getRideId();
        try {
            String json = objectMapper.writeValueAsString(tracking);
            stringRedisTemplate.convertAndSend(topic, json);
            System.out.println("🚕 Publish tracking rideId=" + tracking.getRideId() +
                    " lat=" + tracking.getLatitude() +
                    " lng=" + tracking.getLongitude());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Publish tracking failed", e);
        }
    }
}
