package com.ufanet.meetingsbot.service;

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

    @Transactional
    public Meeting save(Meeting meeting) {
        return meetingRepository.save(meeting);
    }

    @Transactional
    public void createMeeting(Meeting meeting) {
        Account owner = meeting.getOwner();
        log.info("saving meeting by user {}", owner.getId());
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
        meetingRepository.save(meeting);
    }

    public Optional<Meeting> getLastChangedMeetingByOwnerId(long ownerId) {
        return meetingRepository.findByOwnerIdAndStateIsNotIn(ownerId,
                List.of(MeetingState.CONFIRMED, MeetingState.PASSED, MeetingState.AWAITING, MeetingState.CANCELED));
    }

    @Transactional
    public Optional<Meeting> getById(long meetingId) {
        return meetingRepository.findById(meetingId);
    }

    @Transactional
    public void deleteById(Long meetingId) {
        if (meetingId == null) return;
        meetingRepository.deleteById(meetingId);
    }

    public Optional<Meeting> getByMeetingId(long meetingId) {
        return meetingRepository.findById(meetingId);
    }

    public Optional<MeetingTime> getByMeetingIdAndConfirmedState(long meetingId) {
        return meetingTimeRepository.findByMeetingIdAndConfirmed(meetingId);
    }

    public List<Meeting> getMeetingsByUserIdAndStateIn(long userId, List<MeetingState> states) {
        return meetingRepository.findByAccountMeetingsIdOrOwnerIdAndStateIn(userId, states);
    }

}
