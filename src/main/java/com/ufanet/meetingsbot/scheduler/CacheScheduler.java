package com.ufanet.meetingsbot.scheduler;

import com.ufanet.meetingsbot.cache.impl.BotStateCache;
import com.ufanet.meetingsbot.cache.impl.MeetingStateCache;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.mapper.MeetingMapper;
import com.ufanet.meetingsbot.model.BotState;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.repository.BotRepository;
import com.ufanet.meetingsbot.repository.MeetingRepository;
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
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheScheduler {
    private final MeetingRepository meetingRepository;
    private final MeetingStateCache meetingStateCache;
    private final BotStateCache botStateCache;
    private final BotRepository botRepository;
    private final MeetingMapper meetingMapper;
    @Value("${cache.custom.ttl.bot}")
    private long botTtl;
    @Value("${cache.custom.ttl.meeting}")
    private long meetingTtl;

    @Async
    @Scheduled(fixedRate = 10000)
    public void saveMeetingsAndBotStatesFromCache() {
        Map<Long, Meeting> meetingDataCache = new HashMap<>(meetingStateCache.getMeetingStateCache());
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<Long, Meeting> entry : meetingDataCache.entrySet()) {
            long userId = entry.getKey();
            Meeting meeting = entry.getValue();
            LocalDateTime updatedDt = meeting.getUpdatedDt();
            long seconds = ChronoUnit.SECONDS.between(updatedDt, now);

            if (seconds > meetingTtl) {
                MeetingState state = meeting.getState();
                switch (state) {
                    case AWAITING, CONFIRMED, CANCELED, GROUP_SELECT -> meetingStateCache.evict(userId);
                    default -> {
                        meetingStateCache.evict(userId);
                        meetingRepository.save(meeting);
                        log.info("meeting {} with user id {} saved in database", meeting.getId(), userId);
                    }
                }
                log.info("meetingDto {} with user id {} was evicted from cache", meeting.getId(), userId);
            }
        }
    }

    @PreDestroy
    @Transactional
    public void saveCacheValuesBeforeDestroy() {
        System.out.println("--------------- SAVING CACHE VALUES BEFORE DESTROY -----------------");
        Map<Long, BotState> botCache = botStateCache.getBotStateCache();
        Map<Long, Meeting> meetingCache = meetingStateCache.getMeetingStateCache();
        Collection<BotState> botStates = botCache.values();
        Collection<Meeting> meetings = meetingCache.values();

        botRepository.saveAll(botStates);
        meetingRepository.saveAll(meetings);
    }
}
