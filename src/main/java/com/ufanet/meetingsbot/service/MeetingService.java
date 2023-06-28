package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.MeetingStateCache;
import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.exceptions.UserNotFoundException;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import com.ufanet.meetingsbot.repository.MeetingTimeRepository;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.ufanet.meetingsbot.constants.ToggleButton.NEXT;
import static com.ufanet.meetingsbot.constants.ToggleButton.PREV;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final AccountService accountService;
    private final MeetingStateCache meetingStateCache;
    private final MeetingTimeRepository meetingTimeRepository;

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

    @Transactional
    public Meeting getByOwnerIdAndStateNotIn(long ownerId, List<MeetingState> states) {
        Meeting meetingCache = meetingStateCache.get(ownerId);
        if (meetingCache == null) {
            Optional<Meeting> optionalMeeting = meetingRepository.findByOwnerIdAndStateIsNotIn(ownerId, states);
            if (optionalMeeting.isPresent()) {
                Meeting meeting = optionalMeeting.get();
                meetingStateCache.save(ownerId, meeting);
                return meeting;
            } else {
                Account account = accountService.getByUserId(ownerId)
                        .orElseThrow(() -> new UserNotFoundException(ownerId));
                Meeting meeting = new Meeting(account);
                meetingStateCache.save(ownerId, meeting);
                return meeting;
            }
        }
        return meetingCache;
    }

    public void updateSubjectDuration(Meeting meeting, int duration) {
        Subject subject = meeting.getSubject();
        subject.setDuration(duration);
    }

    public void updateGroup(Meeting meeting, long groupId) {
        log.info("updating group {} in meeting", groupId);
        meeting.setGroup(Group.builder().id(groupId).build());
    }

    public void updateParticipants(Meeting meeting, long participantId) {
        log.info("updating participant {} in meeting", participantId);
        Account participant = accountService.getByUserId(participantId)
                .orElseThrow(UserNotFoundException::new);
        Set<AccountMeeting> participants = meeting.getAccountMeetings();

        int currSize = participants.size();
        participants.removeIf(t -> t.getAccount().getId() == participantId);
        int newSize = participants.size();
        if (currSize == newSize) {
            participants.add(AccountMeeting.builder().meeting(meeting).account(participant).build());
        }
        meeting.setAccountMeetings(participants);
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
        meetingStateCache.evict(userId);
    }

    public void saveOnCache(long userId, Meeting meeting) {
        meetingStateCache.save(userId, meeting);
    }

    public void updateMeetingAccountTime(Meeting meeting, long timeId, List<AccountTime> accountTimes) {

        AccountTime accountTime = accountTimes.stream()
                .filter(at -> at.getId() == timeId).findFirst().orElseThrow();

        Status status = accountTime.getStatus();

        switch (status) {
            case CONFIRMED, AWAITING -> accountTime.setStatus(Status.CANCELED);
            case CANCELED -> accountTime.setStatus(Status.CONFIRMED);
        }
        MeetingTime meetingTime = accountTime.getMeetingTime();
        MeetingDate meetingDate = meetingTime.getMeetingDate();

        meetingTime.addAccountTime(accountTime);
        meetingDate.addMeetingTime(meetingTime);
        meeting.addMeetingDate(meetingDate);
        accountService.saveAccountTime(accountTime);
    }

    @Transactional
    public Optional<Meeting> getById(long meetingId) {
        return meetingRepository.findById(meetingId);
    }

    public void updateSubject(Meeting meeting, String title) {
        if (title.length() < 3) {
            throw new IllegalArgumentException("Длина заголовка должна быть больше 5 символов!");
        }
        Subject subject = meeting.getSubject();
        if (subject == null) {
            subject = new Subject();
        }
        subject.setTitle(title);
        subject.setMeeting(meeting);
        meeting.setSubject(subject);
    }

    public void updateQuestion(Meeting meeting, String question) {
        Subject subject = meeting.getSubject();
        Set<String> questions = subject.getQuestions();
        if (questions.contains(question)) {
            questions.remove(question);
        } else {
            questions.add(question);
        }
//        Optional<String> optional = questions.stream()
//                .filter((q) -> q.getTitle().equals(question)).findFirst();
//
//        if (optional.isEmpty()) {
//            Question newQuestion = Question.builder().title(question)
//                    .subject(subject).build();
//            questions.add(newQuestion);
//        } else questions.remove(optional.get());
        subject.setQuestions(questions);
        meeting.setSubject(subject);
    }

    public void updateDate(Meeting meeting, String callback) {
        Set<MeetingDate> meetingDates = meeting.getDates();
        if (!callback.startsWith(NEXT.name()) && !callback.startsWith(PREV.name())) {
            LocalDate localDate = LocalDate.parse(callback);
            Optional<MeetingDate> dateOptional = meetingDates.stream()
                    .filter(t -> t.getDate().isEqual(localDate)).findFirst();

            if (dateOptional.isEmpty()) {
                MeetingDate meetingDate = MeetingDate.builder()
                        .meeting(meeting).date(localDate).build();

                meetingDates.add(meetingDate);
            } else {
                meetingDates.remove(dateOptional.get());
            }
            meeting.setDates(meetingDates);
        }
    }

    public void updateTime(long userId, Meeting meeting, String callback) {
        Account account = accountService.getByUserId(userId).orElseThrow();
        String zoneOffset = account.getZoneId();
        Set<MeetingDate> meetingDates = meeting.getDates();

        ZonedDateTime zonedDateTime = ZonedDateTime.parse(callback);

        LocalDate localDate = zonedDateTime.toLocalDate();

        MeetingDate date = meetingDates.stream().filter(meetingDate -> meetingDate.getDate()
                .isEqual(localDate)).findFirst().orElseThrow();

        Set<MeetingTime> times = date.getMeetingTimes();
        Optional<MeetingTime> meetingTime = times.stream()
                .filter(t -> t.getTimeWithZoneOffset(zoneOffset).isEqual(zonedDateTime)).findFirst();

        if (meetingTime.isEmpty()) {
            MeetingTime time = MeetingTime.builder().meetingDate(date).dateTime(zonedDateTime).build();
            times.add(time);
            date.setMeetingTimes(times);
        } else {
            times.remove(meetingTime.get());
        }
    }

    public void updateAddress(Meeting meeting, String address) {
        meeting.setAddress(address);
    }

    @Transactional
    public void delete(Meeting meeting) {
        meetingRepository.delete(meeting);
        meetingStateCache.evict(meeting.getOwner().getId());
    }

    public Meeting getByMeetingId(long userId, long meetingId) {
        Meeting cache = meetingStateCache.get(userId);
        if (cache == null) {
            Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
            meetingStateCache.save(userId, meeting);
            return meeting;
        } else {
            return save(cache);
        }
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
