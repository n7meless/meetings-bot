package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.MeetingStateCache;
import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.mapper.MeetingConstructor;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import com.ufanet.meetingsbot.repository.MeetingTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final AccountService accountService;
    private final MeetingStateCache meetingStateCache;
    private final MeetingTimeRepository meetingTimeRepository;
    private final MeetingConstructor meetingConstructor;
    private final GroupService groupService;

    @Transactional
    public Meeting save(Meeting meeting) {
        return meetingRepository.save(meeting);
    }

    @Transactional
    public void saveByOwner(Meeting meeting) {
        Account owner = meeting.getOwner();
        log.info("saving meeting by user {}", owner.getId());
        Set<AccountMeeting> accountMeetings = meeting.getAccountMeetings();
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
        accountMeetings.add(AccountMeeting.builder()
                .account(owner).meeting(meeting).build());

        meeting.setAccountMeetings(accountMeetings);
        meetingStateCache.evict(owner.getId());
        meetingRepository.saveAndFlush(meeting);
    }

    //    @Transactional
//    public Meeting getByOwnerIdAndStateNotIn(long ownerId, List<MeetingState> states) {
//        Meeting meetingCache = meetingStateCache.get(ownerId);
//        if (meetingCache == null) {
//            Optional<Meeting> optionalMeeting = meetingRepository.findByOwnerIdAndStateIsNotIn(ownerId, states);
//            if (optionalMeeting.isPresent()) {
//                Meeting meeting = optionalMeeting.get();
//                meetingStateCache.save(ownerId, meeting);
//                return meeting;
//            } else {
//                Account account = accountService.getByUserId(ownerId)
//                        .orElseThrow(() -> new UserNotFoundException(ownerId));
//                Meeting meeting = new Meeting(account);
//                meetingStateCache.save(ownerId, meeting);
//                return meeting;
//            }
//        }
//        return meetingCache;
//    }
    public Optional<Meeting> getLastChangedMeetingByOwnerId(long ownerId) {
        return meetingRepository.findByOwnerIdAndStateIsNotIn(ownerId,
                List.of(MeetingState.CONFIRMED, MeetingState.PASSED, MeetingState.AWAITING, MeetingState.CANCELED));
    }

    public void updateMeetingAccountTimes(long userId, List<AccountTime> accountTimes) {
        for (AccountTime accountTime : accountTimes) {
            Status status = accountTime.getStatus();
            if (status.equals(Status.AWAITING) &&
                    accountTime.getAccount().getId() == userId) {

                accountTime.setStatus(Status.CONFIRMED);
            }
        }
        accountService.saveAccountTimes(accountTimes);
    }

    public void cancelMeeting(Meeting meeting) {
        meeting.removeDateIf((date) -> true);
        meeting.setState(MeetingState.CANCELED);
        meetingRepository.save(meeting);
    }

    public void clearCache(long userId) {
//        meetingStateCache.evict(userId);
    }

    public void saveOnCache(long userId, Meeting meeting) {
//        meetingStateCache.save(userId, meeting);
    }

    @Transactional
    public Optional<Meeting> getById(long meetingId) {
        return meetingRepository.findById(meetingId);
    }

    @Transactional
    public void deleteById(long meetingId) {
        meetingRepository.deleteById(meetingId);
    }

    public Optional<Meeting> getByMeetingId(long meetingId) {
        return meetingRepository.findById(meetingId);
    }

    public Optional<MeetingTime> getByMeetingIdAndConfirmedState(long meetingId) {
        return meetingTimeRepository.findByMeetingIdAndConfirmed(meetingId);
    }

    @Transactional
    public void processConfirmedMeeting(long userId, Meeting meeting, Optional<MeetingTime> confirmed) {
        MeetingTime meetingTime = confirmed.get();
        MeetingDate meetingDate = meetingTime.getMeetingDate();
        meeting.removeDateIf(date -> !date.equals(meetingDate));
        meetingDate.removeTimeIf(time -> !time.equals(meetingTime));
        meeting.setState(MeetingState.CONFIRMED);
        meetingStateCache.evict(userId);
        meetingRepository.save(meeting);
    }

    public List<Meeting> getMeetingsByUserIdAndStateIn(long userId, List<MeetingState> states) {
        return meetingRepository.findByAccountMeetingsIdOrOwnerIdAndStateIn(userId, states);
    }

}
