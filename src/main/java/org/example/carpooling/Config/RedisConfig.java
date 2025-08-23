package org.example.carpooling.Config;

import org.example.carpooling.Service.RedisService.RedisChatSubscriber;
import org.example.carpooling.Service.RedisService.RedisTrackingSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.redis.host:}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.password:}")
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        if (host == null || host.isBlank()) {
            log.warn("⚠️ No Redis host configured. Redis features will be disabled.");
            return null; // app vẫn start bình thường
        }

        try {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(host);
            config.setPort(port);
            if (password != null && !password.isBlank()) {
                config.setPassword(RedisPassword.of(password));
            }
            return new LettuceConnectionFactory(config);
        } catch (Exception e) {
            log.error("❌ Could not connect to Redis. Continuing without Redis...", e);
            return null; // fail safe
        }
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        if (cf == null) return null;
        return new StringRedisTemplate(cf);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        if (cf == null) return null;

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

        if (cf == null) {
            log.warn("⚠️ RedisMessageListenerContainer disabled because Redis is not configured.");
            return null;
        }

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