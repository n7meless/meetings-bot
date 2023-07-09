package com.ufanet.meetingsbot.scheduler;

import com.ufanet.meetingsbot.cache.impl.BotStateCache;
import com.ufanet.meetingsbot.cache.impl.MeetingCache;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.entity.BotState;
import com.ufanet.meetingsbot.entity.Meeting;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.MeetingService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheScheduler {
    private final MeetingService meetingService;
    private final MeetingCache meetingCache;
    private final BotStateCache botStateCache;
    private final BotService botService;
    @Value("${cache.custom.ttl.bot}")
    private long botTtl;
    @Value("${cache.custom.ttl.meeting}")
    private long meetingTtl;

    @Async
    @Scheduled(fixedRate = 5000)
    public void saveMeetingsAndBotStatesFromCache() {
        Map<Long, Meeting> meetingDataCache = new HashMap<>(meetingCache.getMeetingCache());
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<Long, Meeting> entry : meetingDataCache.entrySet()) {
            long userId = entry.getKey();
            Meeting meeting = entry.getValue();
            LocalDateTime updatedDt = meeting.getUpdatedDt();
            long seconds = ChronoUnit.SECONDS.between(updatedDt, now);

            if (seconds > meetingTtl) {
                MeetingState state = meeting.getState();
                switch (state) {
                    case AWAITING, CONFIRMED, CANCELED, GROUP_SELECT -> meetingCache.evict(userId);
                    default -> {
                        meetingCache.evict(userId);
                        meetingService.save(meeting);
                        log.info("meeting {} with user id {} saved in database", meeting.getId(), userId);
                    }
                }
                log.info("meeting {} with user id {} was evicted from meeting cache", meeting.getId(), userId);
            }
        }
        Map<Long, BotState> botDataCache = new HashMap<>(botStateCache.getBotStateCache());
        for (Map.Entry<Long, BotState> entry : botDataCache.entrySet()) {
            long userId = entry.getKey();
            BotState botState = entry.getValue();
            LocalDateTime updatedDt = botState.getUpdatedDt();
            long seconds = ChronoUnit.SECONDS.between(updatedDt, now);

            if (seconds > botTtl) {
                botStateCache.evict(userId);
                botService.save(botState);
                log.info("bot state {} with user id {} was evicted from bot cache", botState.getId(), userId);
            }
        }
    }

    @PreDestroy
    @Transactional
    public void saveCacheBotStateCacheBeforeDestroy() {
        log.info("saving bot state cache before destroy application");
        Map<Long, BotState> botStateCacheMap = botStateCache.getBotStateCache();
        Collection<BotState> unsavedBotStates = botStateCacheMap.values();
        botService.saveAll(unsavedBotStates);

        Map<Long, Meeting> meetingCacheMap = meetingCache.getMeetingCache();
        List<Meeting> unsavedMeetings = meetingCacheMap.values().stream().filter((meeting) ->
                switch (meeting.getState()) {
                    case CONFIRMED, AWAITING, CANCELED, PASSED -> false;
                    default -> true;
                }).toList();
        meetingService.saveAll(unsavedMeetings);
    }
}
