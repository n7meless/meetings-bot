package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.MeetingCacheManager;
import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.AccountTimeRepository;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.ufanet.meetingsbot.constants.ToggleButton.NEXT;
import static com.ufanet.meetingsbot.constants.ToggleButton.PREV;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final AccountService accountService;
    private final MeetingCacheManager meetingCacheManager;
    private final AccountTimeRepository accountTimeRepository;

    public void update(long userId, Meeting meeting, String callback) {
        MeetingState state = meeting.getState();
        if (state != null) {
            switch (state) {
                case GROUP_SELECTION -> updateGroup(meeting, Long.valueOf(callback));
                case PARTICIPANTS_SELECTION -> updateParticipants(meeting, Long.valueOf(callback));
                case SUBJECT_SELECTION -> updateSubject(meeting, callback);
                case SUBJECT_DURATION_SELECTION -> updateSubjectDuration(meeting, callback);
                case QUESTION_SELECTION -> updateQuestion(meeting, callback);
                case DATE_SELECTION -> updateDate(meeting, callback);
                case TIME_SELECTION -> updateTime(meeting, callback);
                case ADDRESS_SELECTION -> updateAddress(meeting, callback);
                //TODO удалить все про встречу
                case CANCELED -> removeByOwnerId(userId);
            }
        } else meeting.setState(MeetingState.GROUP_SELECTION);
        meetingCacheManager.saveData(userId, meeting);
    }

    public void anotherSave(Meeting meeting) {
        meetingRepository.save(meeting);
    }

    @Transactional
    public void save(Meeting meeting) {
        log.info("saving meeting by user {}", meeting.getOwner().getId());
        Set<Account> participants = meeting.getParticipants();
        Set<MeetingDate> dates = meeting.getDates();
        meeting.setState(MeetingState.AWAITING);
        for (MeetingDate date : dates) {
            Set<MeetingTime> times = date.getMeetingTimes();
            for (MeetingTime time : times) {
                Set<AccountTime> accountTimes = new HashSet<>();
                for (Account account : participants) {
                    AccountTime accountTime = AccountTime.builder().account(account)
                            .meetingTime(time).meetingStatus(Status.AWAITING).build();
                    accountTimes.add(accountTime);
                }
                time.setAccountTimes(accountTimes);
                time.setStatus(Status.AWAITING);
            }
        }
        meeting.setParticipants(participants);
        meetingRepository.save(meeting);
    }

    public Meeting getByOwnerIdAndStateNotReady(long ownerId) {
        Meeting meetingCache = meetingCacheManager.getData(ownerId);
        if (meetingCache == null) {
            return meetingRepository.findByOwnerIdAndStateNotReady(ownerId)
                    .orElseGet(() -> createMeeting(ownerId));
        }
        return meetingCache;
    }

    public Meeting getByOwnerId(long ownerId) {
        Meeting meetingCache = meetingCacheManager.getData(ownerId);
        if (meetingCache == null) {
            return meetingRepository.findByOwnerId(ownerId)
                    .orElseGet(() -> createMeeting(ownerId));
        }
        return meetingCache;
    }

    public Meeting createMeeting(long ownerId) {
        Account account = accountService.getByUserId(ownerId).orElseThrow();
        Meeting meeting = Meeting.builder().owner(account)
                .createdDt(LocalDateTime.now())
                .subject(new Subject())
                .group(new Group())
                .dates(new TreeSet<>(Comparator.comparing(MeetingDate::getDate)))
                .participants(new HashSet<>()).build();
        meetingCacheManager.saveData(ownerId, meeting);
        return meeting;
    }

    public void setNextState(Meeting meeting) {
        MeetingState currState = meeting.getState();
        MeetingState[] values = MeetingState.values();
        int current = currState.ordinal();
        MeetingState nextState = values[current + 1];
        meeting.setState(nextState);
    }

    public void updateSubjectDuration(Meeting meeting, String callback) {
        Subject subject = meeting.getSubject();
        subject.setDuration(Integer.parseInt(callback));
    }

    public void updateGroup(Meeting meeting, Long groupId) {
        log.info("updating group {} in meeting", groupId);
        Group group = meeting.getGroup();
        group.setId(groupId);
        meeting.setState(MeetingState.PARTICIPANTS_SELECTION);
    }

    public void updateParticipants(Meeting meeting, Long participantId) {
        log.info("updating participant {} in meeting", participantId);
        Optional<Account> participantOptional = accountService.getByUserId(participantId);
        Set<Account> participants = meeting.getParticipants();

        if (!participantOptional.isEmpty()) {
            Account participant = participantOptional.get();
            if (participants.contains(participant)) {
                participants.remove(participant);
            } else participants.add(participant);
        }

        meeting.setParticipants(participants);
    }

    @Transactional
    public void updateMeetingAccountTime(long userId, Meeting meeting) {

//        AccountTime time = meetingTime.getAccountTimes()
//                .stream().filter(accountTime -> accountTime.getAccount().getId() == userId)
//                .findFirst().orElseThrow();
//
//        MeetingDate meetingDate = meetingTime.getMeetingDate();
//
//        Status status = time.getMeetingStatus();
//
//        switch (status) {
//            case CONFIRMED, AWAITING -> time.setMeetingStatus(Status.CANCELED);
//            case CANCELED -> time.setMeetingStatus(Status.CONFIRMED);
//        }
//
//        meetingTime.addAccountTime(time);
//        meetingDate.addMeetingTime(meetingTime);
//        meeting.addMeetingDate(meetingDate);
//        anotherSave(meeting);
    }

    public void updateSubject(Meeting meeting, String callback) {
        log.info("updating subject '{}' in meeting", callback);
        Subject subject = meeting.getSubject();
        subject.setTitle(callback);
        subject.setMeeting(meeting);
    }

    public void updateQuestion(Meeting meeting, String questionCallback) {
        Subject subject = meeting.getSubject();
        List<Question> questions = subject.getQuestions();
        Optional<Question> optional = questions.stream()
                .filter((q) -> q.getTitle().equals(questionCallback)).findFirst();

        if (optional.isEmpty()) {
            Question question = Question.builder().title(questionCallback).subject(subject).build();
            questions.add(question);
        } else questions.remove(optional.get());
        subject.setQuestions(questions);
        meeting.setSubject(subject);
    }

    @SneakyThrows
    public void updateDate(Meeting meeting, String callback) {
        Set<MeetingDate> meetingDates = meeting.getDates();

        if (!callback.startsWith(NEXT.name()) && !callback.startsWith(PREV.name())) {

            LocalDate localDate = LocalDate.parse(callback, CustomFormatter.DATE_FORMATTER);

            Optional<MeetingDate> dateOptional = meetingDates.stream()
                    .filter(t -> t.getDate().isEqual(localDate)).findFirst();

            if (dateOptional.isEmpty()) {
                MeetingDate meetingDate = MeetingDate.builder().meeting(meeting).date(localDate).build();
                meetingDates.add(meetingDate);
            } else {
                meetingDates.remove(dateOptional.get());
            }
            meeting.setDates(meetingDates);
        }
    }

    public void updateTime(Meeting meeting, String callback) {
        Set<MeetingDate> meetingDates = meeting.getDates();

        LocalDateTime localDateTime = LocalDateTime.parse(callback, CustomFormatter.DATE_TIME_FORMATTER);

        LocalDate localDate = localDateTime.toLocalDate();
        MeetingDate date = meetingDates.stream().filter(meetingDate -> meetingDate.getDate().isEqual(localDate)).findFirst().orElseThrow();
        Set<MeetingTime> times = date.getMeetingTimes();
        Optional<MeetingTime> meetingTime = times.stream().filter(t -> t.getTime().isEqual(localDateTime)).findFirst();
        if (meetingTime.isEmpty()) {
            MeetingTime time = MeetingTime.builder().meetingDate(date).time(localDateTime).build();
            times.add(time);
            date.setMeetingTimes(times);
        } else {
            times.remove(meetingTime.get());
        }
    }

    public void updateAddress(Meeting meeting, String address) {
        meeting.setAddress(address);
        meeting.setState(MeetingState.READY);
    }

    public void removeByOwnerId(long ownerId) {
        //TODO возможность удаления из базы
        meetingCacheManager.clearData(ownerId);
    }

    public Optional<Meeting> getByMeetingId(long meetingId) {
        return meetingRepository.findById(meetingId);
    }
}
