package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.BotStateCache;
import com.ufanet.meetingsbot.cache.impl.MeetingStateCache;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.BotState;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.repository.BotRepository;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import com.ufanet.meetingsbot.repository.MeetingTimeRepository;
import com.ufanet.meetingsbot.service.message.UpcomingReplyMessageService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class CustomScheduler {
    private final MeetingStateCache meetingStateCache;
    private final MeetingRepository meetingRepository;
    private final BotRepository botRepository;
    private final BotStateCache botStateCache;
    private final MeetingTimeRepository meetingTimeRepository;
    private final UpcomingReplyMessageService upcomingReplyMessage;

//    @Async
//    @Scheduled(fixedRate = 120000)
//    public void saveMeetingFromCache() {
//        Map<Long, Meeting> meetingDataCache = new HashMap<>(meetingStateCache.getMeetingStateCache());
//        LocalDateTime now = LocalDateTime.now();
//        for (Map.Entry<Long, Meeting> entry : meetingDataCache.entrySet()) {
//            Long userId = entry.getKey();
//            Meeting meeting = entry.getValue();
//            LocalDateTime updatedDt = meeting.getUpdatedDt();
//            LocalDateTime expirationDt = now;
//            long seconds = ChronoUnit.SECONDS.between(updatedDt, expirationDt);
//
//            if (seconds > 5) {
//                MeetingState state = meeting.getState();
//                switch (state) {
//                    case AWAITING, CONFIRMED, CANCELED -> meetingStateCache.evict(userId);
//                    default -> {
//                        meetingStateCache.evict(userId);
//                        meetingRepository.save(meeting);
//                        log.info("meeting {} with user id {} saved in database", meeting.getId(), userId);
//                    }
//                }
//                log.info("meeting {} with user id {} was evicted from cache", meeting.getId(), userId);
//            }
//        }
////        processConfirmedMeetings();
////        processExpiredMeetings();
//    }

    public void processConfirmedMeetings() {
        ZonedDateTime now = ZonedDateTime.now();
        List<Meeting> comingMeetings = meetingRepository.findConfirmedMeetingsWhereDatesBetween(now, 10);
        for (Meeting meeting : comingMeetings) {
            ZonedDateTime meetingDate = meeting.getDate();
            ZonedDateTime zonedDateTime = meetingDate.withZoneSameInstant(ZoneId.of("UTC+05:00"));
            System.out.println(zonedDateTime);
            System.out.println(now);
            long between = ChronoUnit.MINUTES.between(zonedDateTime.toLocalDateTime(), now);
            upcomingReplyMessage.sendConfirmedComingMeeting(meeting);
            System.out.println(between);
        }
    }

    public void processExpiredMeetings() {
        //TODO найти участников и уведомить
//        ZonedDateTime now = ZonedDateTime.now();
//        List<Meeting> expiredMeetings = meetingRepository.findConfirmedMeetingsWhereDatesLaterThan(now, 90);
//        for (Meeting meeting : expiredMeetings) {
//            log.info("meeting {} expired", meeting.getId());
//            meeting.setState(MeetingState.PASSED);
//            upcomingReplyMessage.sendCommentNotificationParticipants(meeting);
//        }
//        meetingRepository.saveAll(expiredMeetings);
    }

    @PreDestroy
    @Transactional
    public void saveBotStates() {
        log.info("saving bot states before destroy");
        Map<Long, BotState> botCache = botStateCache.getBotStateCache();
//        Map<Long, Meeting> meetingCache = meetingStateCache.getMeetingStateCache();
        Collection<BotState> botStates = botCache.values();
//        Collection<Meeting> meetings = meetingCache.values();
        botRepository.saveAll(botStates);
//        meetingRepository.saveAll(meetings);
    }
}
