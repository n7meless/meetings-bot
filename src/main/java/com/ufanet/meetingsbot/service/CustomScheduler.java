package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.BotStateManager;
import com.ufanet.meetingsbot.cache.impl.MeetingStateCache;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.BotState;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.repository.BotRepository;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class CustomScheduler {
    private final MeetingStateCache meetingStateCache;
    private final MeetingRepository meetingRepository;
    private final BotRepository botRepository;
    private final BotStateManager botStateCache;

    @Async
    @Scheduled(fixedRate = 20000)
    public void saveMeetingFromCache() {
        Map<Long, Meeting> meetingDataCache = new HashMap<>(meetingStateCache.getMeetingStateCache());
        for (Map.Entry<Long, Meeting> entry : meetingDataCache.entrySet()) {
            Long userId = entry.getKey();
            Meeting meeting = entry.getValue();
            LocalDateTime updatedDt = meeting.getUpdatedDt();
            LocalDateTime expirationDt = LocalDateTime.now();
            long seconds = ChronoUnit.SECONDS.between(updatedDt, expirationDt);

            if (seconds > 5) {
                MeetingState state = meeting.getState();
                switch (state) {
                    case AWAITING, CONFIRMED, CANCELED -> meetingStateCache.evict(userId);
                    default -> {
                        meetingStateCache.evict(userId);
                        meetingRepository.save(meeting);
                    }
                }
                log.info("meeting {} with user id {} was evicted from cache", meeting.getId(), userId);
            }
        }
    }

    @PreDestroy
    @Transactional
    public void saveBotStates() {
        System.out.println("--------------- SAVING BOT STATE ---------------");
        Map<Long, BotState> botCache = botStateCache.getBotStateCache();
        Map<Long, Meeting> meetingCache = meetingStateCache.getMeetingStateCache();
        Collection<BotState> botStates = botCache.values();
        Collection<Meeting> meetings = meetingCache.values();
        botRepository.saveAll(botStates);
        meetingRepository.saveAll(meetings);
    }
}
