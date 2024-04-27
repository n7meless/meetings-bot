package com.ufanet.meetingsbot.config.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@RequiredArgsConstructor
public class RedisProperties {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${cache.redis.ttl.user}")
    private long userTtl;

    @Value("${cache.redis.ttl.groupMembers}")
    private long groupMembersTtl;

    @Value("${cache.redis.ttl.group}")
    private long groupTtl;
}
