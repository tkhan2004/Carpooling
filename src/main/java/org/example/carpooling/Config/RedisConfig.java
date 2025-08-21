package org.example.carpooling.Config;

import org.example.carpooling.Service.Imp.RedisServiceImp.RedisTrackingSubscriberImp;
import org.example.carpooling.Service.RedisService.RedisChatSubscriber;
import org.example.carpooling.Service.RedisService.RedisTrackingSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
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

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration());
    }

    // Dùng StringRedisTemplate cho Pub/Sub (topic là chuỗi, payload JSON)
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    // Nếu bạn vẫn cần RedisTemplate<Object, Object> thì giữ lại nhưng nhớ set serializer
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

        // Lắng nghe chat
        container.addMessageListener(chatListenerAdapter, new PatternTopic("chat:room:*"));

        // Lắng nghe tracking
        container.addMessageListener(trackingListenerAdapter, new PatternTopic("tracking:ride:*"));

        return container;
    }

    @Bean
    public MessageListenerAdapter chatListenerAdapter(RedisChatSubscriber subscriber) {
        // map method onMessage(String) của subscriber
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public MessageListenerAdapter trackingListenerAdapter(RedisTrackingSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }
}
