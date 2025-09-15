package com.tr.autos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    // Long을 Set에 깔끔히 넣고 빼려면 Serializer 맞추거나 문자열로 변환
    @Bean
    public RedisTemplate<String, Long> longRedisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Long> t = new RedisTemplate<>();
        t.setConnectionFactory(cf);
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(new GenericToStringSerializer<>(Long.class));
        t.setHashKeySerializer(new StringRedisSerializer());
        t.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));
        t.afterPropertiesSet();
        return t;
    }
}