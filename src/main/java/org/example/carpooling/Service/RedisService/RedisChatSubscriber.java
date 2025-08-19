package org.example.carpooling.Service.RedisService;

public interface RedisChatSubscriber {
    public void onMessage(String message, String channel);
}
