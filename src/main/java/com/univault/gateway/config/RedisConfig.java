package com.univault.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.univault.gateway.registry.InstanceInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;


@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, InstanceInfo> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, InstanceInfo> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }
}
