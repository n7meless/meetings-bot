package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.BotStateCache;
import com.ufanet.meetingsbot.model.BotState;
import com.ufanet.meetingsbot.repository.BotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BotService {
    private final BotRepository botRepository;
    private final BotStateCache botStateCache;

    public BotState getByUserId(long userId) {
        BotState cacheState = botStateCache.get(userId);
        if (cacheState == null) {
            BotState botState = botRepository.findByAccountId(userId)
                    .orElseThrow();
            botStateCache.save(userId, botState);
            return botState;
        }
        return cacheState;
    }

    public void setLastMsgFromUser(long userId, boolean fromUser) {
        BotState botState = getByUserId(userId);
        botState.setMsgFromUser(fromUser);
    }

    @Transactional
    public void save(BotState botState) {
        botRepository.save(botState);
    }

    public void saveCache(long userId, BotState botState) {
        botStateCache.save(userId, botState);
    }

    public void setState(long userId, String state) {
        BotState botState = getByUserId(userId);
        botState.setState(state);
        botStateCache.save(userId, botState);
    }

    public String getState(long userId) {
        return getByUserId(userId).getState();
    }
}
