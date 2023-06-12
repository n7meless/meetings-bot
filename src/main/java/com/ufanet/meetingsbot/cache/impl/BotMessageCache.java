package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class BotMessageCache implements Cache<Integer> {
    private final Map<Long, Integer> messageCache = new HashMap<>();

    @Override
    public void put(Long userId, Integer messageId) {
        messageCache.put(userId, messageId);
    }

    @Override
    public Integer get(Long userId) {
        return messageCache.getOrDefault(userId, 0);
    }

    @Override
    public void evict(Long userId) {
        messageCache.remove(userId);
    }
}
