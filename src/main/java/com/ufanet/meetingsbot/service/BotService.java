package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.BotStateManager;
import com.ufanet.meetingsbot.model.BotState;
import com.ufanet.meetingsbot.repository.BotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "bot_state")
@RequiredArgsConstructor
public class BotService {
    private final BotRepository botRepository;
    private final BotStateManager botStateManager;

    //TODO custom cache
//    @Cacheable(key = "#userId",value = "bot_state", unless = "#result == null")
    public BotState getByUserId(long userId) {
        BotState cacheState = botStateManager.get(userId);
        if (cacheState == null) {
            BotState botState = botRepository.findByAccountId(userId)
                    .orElseThrow();
            botStateManager.save(userId, botState);
            return botState;
        }
        return cacheState;
    }

    public void setLastMsgFromUser(long userId, boolean fromUser) {
        BotState botState = getByUserId(userId);
        botState.setLastFromUser(fromUser);
        save(botState);
    }

    //    @CachePut(key = "#botState.account.id", value = "bot_state")
    public BotState save(BotState botState) {
        return botRepository.save(botState);
    }


    public void saveCache(long userId, BotState botState) {
//        BotState botState = getByUserId(userId);
//        botState.setState(newState);
//        save(botState);
        botStateManager.save(userId, botState);
    }

    public void setState(long userId, Enum<?> newState) {
        BotState botState = getByUserId(userId);
        botState.setState(newState.name());
        botStateManager.save(userId, botState);
    }

    public void setState(long userId, String state) {
        BotState botState = getByUserId(userId);
        botState.setState(state);
        botStateManager.save(userId, botState);
    }

    public String getState(long userId) {
        return getByUserId(userId).getState();
    }
}
