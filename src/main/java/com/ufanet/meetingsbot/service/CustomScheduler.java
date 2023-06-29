package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.BotStateCache;
import com.ufanet.meetingsbot.cache.impl.MeetingStateCache;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.mapper.MeetingConstructor;
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
    private final UpcomingReplyMessageService upcomingReplyMessage;
    private final MeetingConstructor meetingConstructor;

    @Async
    @Scheduled(fixedRate = 5000)
    public void saveMeetingFromCache() {
        Map<Long, MeetingDto> meetingDataCache = new HashMap<>(meetingStateCache.getMeetingStateCache());
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<Long, MeetingDto> entry : meetingDataCache.entrySet()) {
            Long userId = entry.getKey();
            MeetingDto meetingDto = entry.getValue();
            LocalDateTime updatedDt = meetingDto.getUpdatedDt().toLocalDateTime();
            long seconds = ChronoUnit.SECONDS.between(updatedDt, now);
            if (seconds > 5) {
                MeetingState state = meetingDto.getState();
                switch (state) {
                    case AWAITING, CONFIRMED, CANCELED, GROUP_SELECT -> meetingStateCache.evict(userId);
                    default -> {
                        meetingStateCache.evict(userId);
                        Meeting meeting = meetingConstructor.mapToEntity(meetingDto);
                        meetingRepository.save(meeting);
                        log.info("meeting {} with user id {} saved in database", meetingDto.getId(), userId);
                    }
                }
                log.info("meetingDto {} with user id {} was evicted from cache", meetingDto.getId(), userId);
            }
        }
//        processConfirmedMeetings();
//        processExpiredMeetings();
    }

    public void processConfirmedMeetings() {
        ZonedDateTime now = ZonedDateTime.now();
        List<MeetingDto> meetings = meetingRepository.findConfirmedMeetingsWhereDatesBetween(now, 10)
                .stream().map(meetingConstructor::mapToDto).toList();
        for (MeetingDto meetingDto : meetings) {
            ZonedDateTime meetingDate = meetingDto.getDate();
            ZonedDateTime zonedDateTime = meetingDate.withZoneSameInstant(ZoneId.of("UTC+05:00"));
            System.out.println(zonedDateTime);
            System.out.println(now);
            long between = ChronoUnit.MINUTES.between(zonedDateTime.toLocalDateTime(), now);
            upcomingReplyMessage.sendConfirmedComingMeeting(meetingDto);
            System.out.println(between);
        }
    }

    public void processExpiredMeetings() {
        //TODO найти участников и уведомить
        ZonedDateTime now = ZonedDateTime.now();
        List<Meeting> expiredMeetings = meetingRepository.findConfirmedMeetingsWhereDatesLaterThan(now, 90);
        for (Meeting meeting : expiredMeetings) {
            meeting.setState(MeetingState.PASSED);
            log.info("notification leave feedback about the meeting {}", meeting.getId());
            MeetingDto meetingDto = meetingConstructor.mapToDto(meeting);
            upcomingReplyMessage.sendCommentNotificationParticipants(meetingDto);
        }
        meetingRepository.saveAll(expiredMeetings);
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
