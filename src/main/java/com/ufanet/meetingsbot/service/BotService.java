package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.BotStateCache;
import com.ufanet.meetingsbot.entity.BotState;
import com.ufanet.meetingsbot.exceptions.CustomTelegramApiException;
import com.ufanet.meetingsbot.repository.BotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotService {
    private final BotRepository botRepository;
    private final BotStateCache botStateCache;

    public BotState getByUserId(long userId) {
        BotState cacheState = botStateCache.get(userId);
        if (cacheState == null) {
            log.info("getting botState by user {} from db", userId);
            BotState botState = botRepository.findByAccountId(userId).orElseThrow(() -> new CustomTelegramApiException(userId, "error.internal.exception"));
            botStateCache.save(userId, botState);
            return botState;
        }
        return cacheState;
    }

    public void setLastMsgFromUser(long userId, boolean fromUser) {
        BotState botState = getByUserId(userId);
        botState.setMsgFromUser(fromUser);
        saveCache(userId, botState);
    }

    @Transactional
    public void save(BotState botState) {
        log.info("saving botState {} into db", botState.getId());
        botRepository.save(botState);
    }

    public void saveCache(long userId, BotState botState) {
        botState.setUpdatedDt(LocalDateTime.now());
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

    @Transactional
    public Collection<BotState> saveAll(Collection<BotState> botStates) {
        log.info("saving bot states {} into db", botStates);
        return botRepository.saveAll(botStates);
    }
}
