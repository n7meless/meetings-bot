package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.BotStateCache;
import com.ufanet.meetingsbot.cache.impl.MeetingDtoStateCache;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.mapper.MeetingMapper;
import com.ufanet.meetingsbot.message.UpcomingReplyMessage;
import com.ufanet.meetingsbot.model.BotState;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.repository.BotRepository;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class CustomScheduler {
    private final MeetingDtoStateCache meetingDtoStateCache;
    private final MeetingRepository meetingRepository;
    private final BotStateCache botStateCache;
    private final BotRepository botRepository;
    private final UpcomingReplyMessage upcomingReplyMessage;
    private final MeetingMapper meetingMapper;

    @Value("${cache.custom.ttl.bot}")
    private long botTtl;
    @Value("${cache.custom.ttl.meeting}")
    private long meetingTtl;

    @Async
    @Scheduled(fixedRate = 10000)
    public void saveMeetingsAndBotStatesFromCache() {
        Map<Long, MeetingDto> meetingDataCache = new HashMap<>(meetingDtoStateCache.getMeetingStateCache());
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<Long, MeetingDto> entry : meetingDataCache.entrySet()) {
            long userId = entry.getKey();
            MeetingDto meetingDto = entry.getValue();
            LocalDateTime updatedDt = meetingDto.getUpdatedDt();
            long seconds = ChronoUnit.SECONDS.between(updatedDt, now);

            if (seconds > meetingTtl) {
                MeetingState state = meetingDto.getState();
                switch (state) {
                    case AWAITING, CONFIRMED, CANCELED, GROUP_SELECT -> meetingDtoStateCache.evict(userId);
                    default -> {
                        meetingDtoStateCache.evict(userId);
                        Meeting meeting = meetingMapper.map(meetingDto);
                        meetingRepository.save(meeting);
                        log.info("meeting {} with user id {} saved in database", meetingDto.getId(), userId);
                    }
                }
                log.info("meetingDto {} with user id {} was evicted from cache", meetingDto.getId(), userId);
            }
        }

        Map<Long, BotState> botDataCache = new HashMap<>(botStateCache.getBotStateCache());
        for (Map.Entry<Long, BotState> entry : botDataCache.entrySet()) {
            long userId = entry.getKey();
            BotState botState = entry.getValue();
            LocalDateTime botUpdatedDt = botState.getUpdatedDt();
            long seconds = ChronoUnit.SECONDS.between(botUpdatedDt, now);
            if (seconds > botTtl) {
                log.info("saving bot state {} into db", botState.getId());
                botRepository.save(botState);
                botStateCache.evict(userId);
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void checkMeetings() {
        checkUpcomingMeetings();
        checkExpiredMeetings();
    }

    private void checkUpcomingMeetings() {
        ZonedDateTime now = ZonedDateTime.now();
        List<Meeting> meetings = meetingRepository.findConfirmedMeetingsWhereDatesBetween(now, 10);
        for (Meeting meeting : meetings) {
            LocalDateTime updatedDt = meeting.getUpdatedDt();
            long between = ChronoUnit.MINUTES.between(updatedDt, now);
            if (between > 10) {
                upcomingReplyMessage.sendConfirmedComingMeeting(meeting);
                meeting.setUpdatedDt(LocalDateTime.now());
                meetingRepository.save(meeting);
            }
        }
    }

    private void checkExpiredMeetings() {
        ZonedDateTime now = ZonedDateTime.now();
//        List<Meeting> expiredMeetings = meetingRepository.findConfirmedMeetingsWhereDatesLaterThan(now, 90);
        List<Meeting> expiredMeetings = meetingRepository.findConfirmedMeetingsWhereDatesLaterThanSubjectDuration(now);
        for (Meeting meeting : expiredMeetings) {
            meeting.setState(MeetingState.PASSED);
            log.info("notification leave feedback about the meeting {}", meeting.getId());
            upcomingReplyMessage.sendCommentNotificationParticipants(meeting);
        }
        meetingRepository.saveAll(expiredMeetings);
    }

    @PreDestroy
    @Transactional
    public void saveCacheValuesBeforeDestroy() {
        System.out.println("--------------- SAVING CACHE VALUES BEFORE DESTROY -----------------");
        Map<Long, BotState> botCache = botStateCache.getBotStateCache();
        Map<Long, MeetingDto> meetingCache = meetingDtoStateCache.getMeetingStateCache();
        Collection<BotState> botStates = botCache.values();
        List<Meeting> meetings = meetingCache.values().stream()
                .map(meetingMapper::map).collect(Collectors.toList());

        botRepository.saveAll(botStates);
        meetingRepository.saveAll(meetings);
    }
}
