package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import com.ufanet.meetingsbot.model.BotState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class BotStateCache implements Cache<BotState> {
    private final Map<Long, BotState> botStateCache = new HashMap<>();

    @Override
    public void save(Long userId, BotState state) {
        log.info("saving bot state {} in cache by user {}", state.getId(), userId);
        botStateCache.put(userId, state);
    }

    @Override
    public BotState get(Long userId) {
        log.info("getting bot state from cache by user {}", userId);
        return botStateCache.getOrDefault(userId, null);
    }

    @Override
    public void evict(Long userId) {
        log.info("evict bot state cache by user {}", userId);
        botStateCache.remove(userId);
    }
}
