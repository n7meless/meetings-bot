package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.MeetingCacheManager;
import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import com.ufanet.meetingsbot.repository.MeetingTimeRepository;
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
    private final MeetingTimeRepository meetingTimeRepository;

    public void update(long userId, Meeting meeting, String callback) {
        MeetingState state = meeting.getState();
        if (state != null) {
            switch (state) {
                case GROUP_SELECT -> updateGroup(meeting, Long.valueOf(callback));
                case PARTICIPANT_SELECT -> updateParticipants(meeting, Long.valueOf(callback));
                case SUBJECT_SELECT -> updateSubject(meeting, callback);
                case SUBJECT_DURATION_SELECT -> updateSubjectDuration(meeting, callback);
                case QUESTION_SELECT -> updateQuestion(meeting, callback);
                case DATE_SELECT -> updateDate(meeting, callback);
                case TIME_SELECT -> updateTime(meeting, callback);
                case ADDRESS_SELECT -> updateAddress(meeting, callback);
                //TODO удалить все про встречу
                case CANCELED -> removeByOwnerId(userId);
            }
        } else meeting.setState(MeetingState.GROUP_SELECT);
        meetingCacheManager.saveData(userId, meeting);
        meeting.setUpdatedDt(LocalDateTime.now());

    }

    public List<Meeting> getMeetingsByOwnerIdOrParticipantsId(long userId) {
        return meetingRepository.findMeetingsByParticipantsIdOrOwnerIdEquals(userId, userId);
    }

    public void anotherSave(Meeting meeting) {
        meetingRepository.save(meeting);
    }

    @Transactional
    public void updateAccountTimes(Meeting meeting) {
        log.info("saving meeting by user {}", meeting.getOwner().getId());
        Set<Account> participants = meeting.getParticipants();
        Set<MeetingDate> dates = meeting.getDates();
        meeting.setState(MeetingState.AWAITING);
        dates.removeIf(meetingDate -> meetingDate.getMeetingTimes().isEmpty());

        for (MeetingDate date : dates) {
            Set<MeetingTime> times = date.getMeetingTimes();
            for (MeetingTime time : times) {
                Set<AccountTime> accountTimes = new HashSet<>();
                for (Account account : participants) {
                    AccountTime accountTime = AccountTime.builder().account(account)
                            .meetingTime(time).meetingStatus(Status.AWAITING).build();
                    accountTimes.add(accountTime);
//                    account.updateBotState(AccountState.EDIT);
                }
                time.setAccountTimes(accountTimes);
            }
        }
        meeting.setParticipants(participants);
        meetingRepository.save(meeting);
        meetingCacheManager.clearData(meeting.getOwner().getId());
    }

    public Meeting getByOwnerIdAndStateNotReady(long ownerId) {
        Meeting meetingCache = meetingCacheManager.getData(ownerId);
        if (meetingCache == null) {
            Meeting meeting = meetingRepository.findByOwnerIdAndStateIsNotIn(ownerId, List.of(MeetingState.CONFIRMED, MeetingState.AWAITING, MeetingState.CANCELED))
                    .orElseGet(() -> createMeeting(ownerId));
            meetingCacheManager.saveData(ownerId, meeting);
            return meeting;
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
        Meeting meeting = new Meeting();
        meeting.setCreatedDt(LocalDateTime.now());
        meeting.setUpdatedDt(LocalDateTime.now());
        meeting.setOwner(account);
//        meeting.setGroup(new Group());
        meeting.setSubject(new Subject());
        meeting.setDates(new TreeSet<>(Comparator.comparing(MeetingDate::getDate)));
        meeting.setParticipants(new HashSet<>());

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
        if (group == null) {
            group = new Group();
        }
        group.setId(groupId);
        meeting.setGroup(group);
        meeting.setState(MeetingState.PARTICIPANT_SELECT);
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

    public void updateMeetingAccountTimes(long userId, List<AccountTime> accountTimes, boolean canceled) {
        for (AccountTime accountTime : accountTimes) {
            if (canceled) {
                accountTime.setMeetingStatus(Status.CANCELED);
            } else {
                Status status = accountTime.getMeetingStatus();
                if (status.equals(Status.AWAITING)) {
                    accountTime.setMeetingStatus(Status.CONFIRMED);
                }
            }
        }
        accountService.saveAccountTimes(accountTimes);
    }

    public void updateMeetingAccountTime(long userId, Meeting meeting, long timeId, List<AccountTime> accountTimes) {

        AccountTime accountTime = accountTimes.stream().filter(at -> at.getId() == timeId)
                .findFirst().orElseThrow();

        Status status = accountTime.getMeetingStatus();

        switch (status) {
            case CONFIRMED, AWAITING -> accountTime.setMeetingStatus(Status.CANCELED);
            case CANCELED -> accountTime.setMeetingStatus(Status.CONFIRMED);
        }
        MeetingTime meetingTime = accountTime.getMeetingTime();
        MeetingDate meetingDate = meetingTime.getMeetingDate();

        meetingTime.addAccountTime(accountTime);
        meetingDate.addMeetingTime(meetingTime);
        meeting.addMeetingDate(meetingDate);
        meetingCacheManager.saveData(userId, meeting);
        accountService.saveAccountTime(accountTime);
    }

    @Transactional
    public Optional<Meeting> getById(long meetingId) {
        return meetingRepository.findById(meetingId);
    }

    public void updateSubject(Meeting meeting, String callback) {
        log.info("updating subject '{}' in meeting", callback);
        Subject subject = meeting.getSubject();
        subject.setTitle(callback);
        subject.setMeeting(meeting);
        meeting.setState(MeetingState.QUESTION_SELECT);
    }

    public void updateQuestion(Meeting meeting, String questionCallback) {
        Subject subject = meeting.getSubject();
        Set<Question> questions = subject.getQuestions();
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
        Meeting meeting = meetingCacheManager.getData(ownerId);
        if (meeting != null) {
            meetingRepository.delete(meeting);
        } else {
            meetingRepository.deleteByOwnerId(ownerId);
        }
        meetingCacheManager.clearData(ownerId);
    }

    public Meeting getByMeetingIdOrUserId(long userId, long meetingId) {
        Meeting cache = meetingCacheManager.getData(userId);
        if (cache == null) {
            Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
            meetingCacheManager.saveData(userId, meeting);
            return meeting;
        }
        return cache;
    }

    public List<MeetingTime> getByMeetingIdAndConfirmedState(long meetingId) {
        return meetingTimeRepository.findByMeetingIdAndConfirmed(meetingId);
    }

    public void processConfirmedMeeting(Meeting meeting, List<MeetingTime> confirmed) {
        MeetingTime meetingTime = confirmed.get(0);
        MeetingDate meetingDate = meetingTime.getMeetingDate();
        meetingDate.setMeetingTimes(Set.of(meetingTime));
        meeting.setDates(Set.of(meetingDate));
        meeting.setState(MeetingState.CONFIRMED);
        anotherSave(meeting);
    }

    public List<Meeting> getMeetingsByUserIdAndState(long userId, MeetingState state) {
        return meetingRepository.findByParticipantsIdOrOwnerIdAndStateEquals(userId, userId, state);
    }

}
