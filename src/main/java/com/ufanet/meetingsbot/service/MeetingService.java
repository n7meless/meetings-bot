package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.MeetingCacheManager;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
                case ADDRESS_SELECTION -> {
                    updateAddress(meeting, callback);
                }
                case CANCELED -> removeByOwnerId(userId);
            }
        } else meeting.setState(MeetingState.GROUP_SELECTION);
        meetingCacheManager.saveData(userId, meeting);
    }


    public void save(Meeting meeting) {
        log.info("saving meeting by user {}", meeting.getOwner().getId());
        meetingRepository.save(meeting);
    }

    public Meeting getByOwnerId(long ownerId) {
        Meeting meetingCache = meetingCacheManager.getData(ownerId);
        if (meetingCache == null) {
            return meetingRepository.findByOwnerId(ownerId)
                    .orElseGet(() -> createNewMeeting(ownerId));
        }
        return meetingCache;
    }

    public Meeting createNewMeeting(long ownerId) {
        Account account = accountService.getByUserId(ownerId).orElseThrow();
        Meeting meeting = Meeting.builder().owner(account)
                .subject(new Subject()).group(new Group())
                .dates(new ArrayList<>())
                .participants(new HashSet<>()).build();
        meetingCacheManager.saveData(ownerId, meeting);
        return meeting;
    }

    public void setNextState(long userId) {
        Meeting meeting = getByOwnerId(userId);
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

    public void setState(Meeting meeting, MeetingState state) {
        meeting.setState(state);
//        meetingCacheManager.saveData();
    }

    public void updateSubject(Meeting meeting, String callback) {
        log.info("updating subject '{}' in meeting", callback);
        Subject subject = meeting.getSubject();
        subject.setTitle(callback);
    }

    public void updateQuestion(Meeting meeting, String question) {
        Subject subject = meeting.getSubject();
        List<Question> questions = subject.getQuestions();
        Optional<Question> optional = questions.stream().filter((q) -> q.getTitle().equals(question)).findFirst();
        if (optional.isEmpty()) {
            questions.add(Question.builder().title(question).build());
        } else questions.remove(optional.get());
        subject.setQuestions(questions);
        meeting.setSubject(subject);
    }

    @SneakyThrows
    public void updateDate(Meeting meeting, String callback) {
        DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<MeetingDate> dates = meeting.getDates();

        if (!callback.startsWith(NEXT.name()) && !callback.startsWith(PREV.name())) {
            LocalDate date = LocalDate.parse(callback, ofPattern);

            Optional<MeetingDate> dateOptional = dates.stream()
                    .filter((meetingDate) -> meetingDate.getDate().equals(date)).findFirst();

            if (dateOptional.isEmpty()) {
                dates.add(MeetingDate.builder().date(date).build());
            } else {
                dates.remove(dateOptional.get());
            }
            meeting.setDates(dates);
        }
    }

    public void updateTime(Meeting meeting, String callback) {

        DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        List<MeetingDate> dates = meeting.getDates();

        LocalDateTime localDateTime = LocalDateTime.parse(callback, ofPattern);
        Optional<MeetingDate> meetingDate = dates.stream()
                .filter((date) -> date.getDate().isEqual(localDateTime.toLocalDate())).findFirst();

        if (!meetingDate.isEmpty()) {
            MeetingDate date = meetingDate.get();
            List<MeetingTime> times = date.getTime();
            Optional<MeetingTime> meetingTime = times.stream().filter((time) -> LocalDateTime.of(date.getDate(), time.getTime()).isEqual(localDateTime)).findFirst();
            if (!meetingTime.isEmpty()) {
                times.remove(meetingTime.get());
            } else {
                MeetingTime time = MeetingTime.builder().date(date).time(localDateTime.toLocalTime()).build();
                times.add(time);
                date.setTime(times);
            }
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
}
