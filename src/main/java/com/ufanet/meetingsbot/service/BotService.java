package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.model.BotState;
import com.ufanet.meetingsbot.repository.BotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
@Service
@CacheConfig(cacheNames = "bot_state")
@RequiredArgsConstructor
public class BotService {
    private final BotRepository botRepository;
    @Cacheable(key = "#userId",value = "bot_state", unless = "#result == null")
    public BotState getByUserId(long userId){
        return botRepository.findByAccountId(userId).orElseThrow();
    }
    @CachePut(key = "#botState.account.id", value = "bot_state")
    public BotState save(BotState botState){
        return botRepository.save(botState);
    }
}
