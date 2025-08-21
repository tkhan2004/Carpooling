package org.example.carpooling.Service.RedisService;


import com.fasterxml.jackson.core.JsonProcessingException;

public interface RedisTrackingSubscriber {
    public void onMessage(String message, String channel) throws JsonProcessingException;
}
