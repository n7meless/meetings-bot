package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import com.ufanet.meetingsbot.constants.state.AccountState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AccountStateCache implements Cache<AccountState> {
    private final Map<Long, AccountState> botStates = new HashMap<>();

    @Override
    public void put(Long userId, AccountState accountState) {
        botStates.put(userId, accountState);
    }

    @Override
    public AccountState get(Long userId) {
        return botStates.getOrDefault(userId, AccountState.CREATE);
    }

    @Override
    public void evict(Long userId) {
        botStates.remove(userId);
    }
}
