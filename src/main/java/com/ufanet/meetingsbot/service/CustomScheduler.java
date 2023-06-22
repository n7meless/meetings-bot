package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.MeetingCacheManager;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.Meeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class CustomScheduler {
    private final MeetingCacheManager meetingCacheManager;
    private final MeetingService meetingService;

    @Async
    @Scheduled(fixedRate = 10000)
    public void saveMeetingFromCache() {
        Map<Long, Meeting> meetingDataCache = new HashMap<>(meetingCacheManager.getMeetingDataCache());
        for (Map.Entry<Long, Meeting> entry : meetingDataCache.entrySet()) {
            Long userId = entry.getKey();
            Meeting meeting = entry.getValue();
            LocalDateTime updatedDt = meeting.getUpdatedDt();
            LocalDateTime expirationDt = LocalDateTime.now();
            long seconds = ChronoUnit.SECONDS.between(updatedDt, expirationDt);

            if (seconds > 5) {
                MeetingState state = meeting.getState();
                switch (state) {
                    case AWAITING, CONFIRMED, CANCELED -> meetingCacheManager.evict(userId);
                    default -> {
                        meetingService.save(meeting);
                        meetingCacheManager.evict(userId);
                    }
                }
                log.info("meeting {} with user id {} was evicted from cache", meeting.getId(), userId);
            }
        }
    }
}
