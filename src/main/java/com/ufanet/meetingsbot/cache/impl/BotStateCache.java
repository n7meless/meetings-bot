package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import com.ufanet.meetingsbot.state.BotState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BotStateCache implements Cache<BotState> {
    private final Map<Long, BotState> botStates = new HashMap<>();

    @Override
    public void put(Long userId, BotState botState) {
        botStates.put(userId, botState);
    }

    @Override
    public BotState get(Long userId) {
        if (botStates.containsKey(userId))
            return botStates.get(userId);
        return null;
    }

    @Override
    public void evict(Long userId) {
        botStates.remove(userId);
    }
}
