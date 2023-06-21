package com.ufanet.meetingsbot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Slf4j
@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;
    @Value("${redis.ttl.user}")
    private long userTtl;
    @Value("${redis.ttl.meeting}")
    private long meetingTtl;

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {

        RedisStandaloneConfiguration redisStandaloneConfiguration =
                new RedisStandaloneConfiguration(host, port);
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, ?> redisTemplate() {
        RedisTemplate<String, ?> template = new RedisTemplate<>();

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
//                .initialCacheNames(Set.of("user", "users"))
                .withCacheConfiguration("account",
                        RedisCacheConfiguration.defaultCacheConfig()
//                                .disableCachingNullValues()
                                .entryTtl(Duration.ofSeconds(userTtl)))
                .withCacheConfiguration("meeting",
                        RedisCacheConfiguration.defaultCacheConfig()
//                                .disableCachingNullValues()
                                .entryTtl(Duration.ofSeconds(meetingTtl)))
                .withCacheConfiguration("group_members",
                        RedisCacheConfiguration.defaultCacheConfig()
//                                .disableCachingNullValues()
                                .entryTtl(Duration.ofSeconds(10)))
                .withCacheConfiguration("bot_state",
                        RedisCacheConfiguration.defaultCacheConfig()
//                                .disableCachingNullValues()
                                .entryTtl(Duration.ofSeconds(60)));
    }

}