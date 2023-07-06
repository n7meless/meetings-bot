package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import com.ufanet.meetingsbot.entity.BotState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Service
@RequiredArgsConstructor
public class BotStateCache implements Cache<BotState> {

    private final Map<Long, BotState> botStateCache = new ConcurrentHashMap<>();

    @Override
    public void save(Long userId, BotState state) {
        botStateCache.put(userId, state);
    }

    @Override
    public BotState get(Long userId) {
        return botStateCache.getOrDefault(userId, null);
    }

    @Override
    public void evict(Long userId) {
        botStateCache.remove(userId);
    }
}
