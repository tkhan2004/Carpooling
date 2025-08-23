package org.example.carpooling.Config;

import org.example.carpooling.Service.RedisService.RedisChatSubscriber;
import org.example.carpooling.Service.RedisService.RedisTrackingSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.URI;

@Configuration
public class RedisConfig {

    @Value("${SPRING_REDIS_URL}")
    private String redisUrl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        try {
            URI uri = new URI(redisUrl);
            String[] userInfo = uri.getUserInfo().split(":");

            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(uri.getHost());
            config.setPort(uri.getPort());
            if (userInfo.length > 1) {
                config.setPassword(RedisPassword.of(userInfo[1]));
            }

            return new LettuceConnectionFactory(config);
        } catch (Exception e) {
            throw new RuntimeException("❌ Invalid Redis URL: " + redisUrl, e);
        }
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Object> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);
        tpl.setKeySerializer(new StringRedisSerializer());
        tpl.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        tpl.setHashKeySerializer(new StringRedisSerializer());
        tpl.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        tpl.afterPropertiesSet();
        return tpl;
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory cf,
            MessageListenerAdapter chatListenerAdapter,
            MessageListenerAdapter trackingListenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(cf);
        container.addMessageListener(chatListenerAdapter, new PatternTopic("chat:room:*"));
        container.addMessageListener(trackingListenerAdapter, new PatternTopic("tracking:ride:*"));
        return container;
    }

    @Bean
    public MessageListenerAdapter chatListenerAdapter(RedisChatSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public MessageListenerAdapter trackingListenerAdapter(RedisTrackingSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }
}