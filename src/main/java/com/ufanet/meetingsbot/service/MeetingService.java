package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.MeetingStateCache;
import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import com.ufanet.meetingsbot.repository.MeetingTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final MeetingTimeRepository meetingTimeRepository;
    private final MeetingStateCache meetingStateCache;

    @Transactional
    public void save(Meeting meeting) {
        log.info("saving meeting {} with owner {} into db", meeting.getId(), meeting.getOwner().getId());
        meetingRepository.save(meeting);
    }

    public void saveOnCache(Long userId, Meeting meeting) {
        meetingStateCache.save(userId, meeting);
    }

    public Optional<Meeting> getFromCache(Long userId) {
        Meeting meeting = meetingStateCache.get(userId);
        return Optional.ofNullable(meeting);
    }

    public void clearCache(Long userId) {
        meetingStateCache.evict(userId);
    }

    @Transactional
    public void createMeeting(Meeting meeting) {
        Account owner = meeting.getOwner();
        log.info("creating meeting by user {}", owner.getId());

        Set<AccountMeeting> accountMeetings = meeting.getAccountMeetings()
                .stream().filter((am) -> !Objects.equals(am.getAccount().getId(), meeting.getOwner().getId()))
                .collect(Collectors.toSet());

        Set<MeetingDate> dates = meeting.getDates();
        meeting.setState(MeetingState.AWAITING);
        dates.removeIf(meetingDate -> meetingDate.getMeetingTimes().isEmpty());

        for (MeetingDate date : dates) {
            Set<MeetingTime> times = date.getMeetingTimes();
            for (MeetingTime time : times) {
                Set<AccountTime> accountTimes = new HashSet<>();
                for (AccountMeeting accountMeeting : accountMeetings) {
                    Account account = accountMeeting.getAccount();
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

}
