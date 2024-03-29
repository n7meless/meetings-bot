package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.MeetingCache;
import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.entity.*;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import com.ufanet.meetingsbot.repository.MeetingTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final MeetingTimeRepository meetingTimeRepository;
    private final MeetingCache meetingCache;

    @Transactional
    public Meeting save(Meeting meeting) {
        log.info("saving meeting {} into db", meeting.getId());
        return meetingRepository.save(meeting);
    }

    @Transactional
    public void createMeeting(Meeting meeting) {
        Account owner = meeting.getOwner();
        log.info("creating meeting by user {}", owner.getId());

        Set<Account> participants = meeting.getParticipants();
        participants.removeIf((am) -> Objects.equals(am.getId(), meeting.getOwner().getId()));

        Set<MeetingDate> dates = meeting.getDates();
        meeting.setState(MeetingState.AWAITING);
        dates.removeIf(meetingDate -> meetingDate.getMeetingTimes().isEmpty());

        for (MeetingDate date : dates) {
            Set<MeetingTime> times = date.getMeetingTimes();
            for (MeetingTime time : times) {
                Set<AccountTime> accountTimes = new HashSet<>();
                for (Account account : participants) {
                    AccountTime accountTime = AccountTime.builder().account(account)
                            .meetingTime(time).status(Status.AWAITING).build();
                    accountTimes.add(accountTime);
                }
                time.setAccountTimes(accountTimes);
            }
        }
        save(meeting);
    }

    public Optional<Meeting> getLastChangedMeetingByOwnerId(long ownerId) {
        log.info("getting meeting by user {} from db", ownerId);
        return meetingRepository.findByOwnerIdAndStateIsNotIn(ownerId,
                List.of(MeetingState.CONFIRMED, MeetingState.PASSED, MeetingState.AWAITING, MeetingState.CANCELED));
    }

    @Transactional
    public void deleteById(Long meetingId) {
        if (meetingId == null) return;
        log.info("deleting meeting {} from db", meetingId);
        meetingRepository.deleteById(meetingId);
    }

    public Optional<Meeting> getByMeetingId(long meetingId) {
        log.info("getting meeting {} from db", meetingId);
        return meetingRepository.findById(meetingId);
    }

    public Optional<MeetingTime> getByMeetingIdAndConfirmedState(long meetingId) {
        log.info("getting confirmed meeting {} from db", meetingId);
        return meetingTimeRepository.findByMeetingIdAndAllInStatus(meetingId, Status.CONFIRMED);
    }

    public List<Meeting> getMeetingsByUserIdAndStateIn(long userId, List<MeetingState> states) {
        log.info("getting meetings by user {} with states {} from db", userId, states);
        return meetingRepository.findMeetingsByUserIdAndStateIn(userId, states);
    }

    public List<Meeting> getConfirmedMeetingsWhereDateMinutesBetween(ZonedDateTime now, int endValue) {
        log.info("getting confirmed meetings where current date and meeting date between 0 and {} min", endValue);
        return meetingRepository.findConfirmedMeetingsWhereDateMinutesBetween(now, endValue);
    }

    public List<Meeting> saveAll(List<Meeting> meetings) {
        log.info("saving meetings '{}' into db", meetings.stream().map(Meeting::getId).map(String::valueOf)
                .collect(Collectors.joining(",")));
        return meetingRepository.saveAll(meetings);
    }

    public List<Meeting> getConfirmedMeetingsWhereDatesLaterThanSubjectDuration(ZonedDateTime now) {
        log.info("getting confirmed meetings where current date {} and meeting date later than subject duration", now);
        return meetingRepository.findConfirmedMeetingsWhereDatesLaterThanSubjectDuration(now);
    }

    public void saveOnCache(Long userId, Meeting meeting) {
        meetingCache.save(userId, meeting);
    }

    public Meeting getFromCache(Long userId) {
        return meetingCache.get(userId);
    }

    public void clearCache(Long userId) {
        meetingCache.evict(userId);
    }
}
