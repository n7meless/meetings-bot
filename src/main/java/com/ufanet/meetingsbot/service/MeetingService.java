package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.MeetingCacheManager;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import com.ufanet.meetingsbot.state.MeetingState;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final MeetingCacheManager meetingCacheManager;


    public void save(Meeting meeting) {
        log.info("saving meeting by user {}", meeting.getOwner().getId());
        meetingRepository.save(meeting);
    }


    public Meeting getByOwnerId(long ownerId) {
        Meeting meetingCache = meetingCacheManager.getData(ownerId);
        if (meetingCache == null) {
            Optional<Meeting> meetingOptional = meetingRepository.findByOwnerId(ownerId);
            if (meetingOptional.isEmpty()) {
                Optional<Account> accountOptional = accountService.getByUserId(ownerId);
                return Meeting.builder()
                        .owner(accountOptional.get())
                        .group(new Group())
                        .subject(new Subject()).build();
            } else {
                Meeting meeting = meetingOptional.get();
                meetingCacheManager.saveData(ownerId, meeting);
                return meeting;
            }
        }
        return meetingCache;
    }

    public void setNextState(long userId) {
        Meeting meeting = getByOwnerId(userId);
        MeetingState currState = meeting.getState();
        MeetingState[] values = MeetingState.values();
        int current = currState.ordinal();
        MeetingState nextState = values[current + 1];
        meeting.setState(nextState);
    }

    public void update(long userId, String callback) {
        Meeting meeting = getByOwnerId(userId);
        MeetingState state = meeting.getState();
        if (state != null) {
            switch (state) {
                case GROUP_SELECTION -> {
                    updateGroup(meeting, Long.valueOf(callback));
                    setNextState(userId);
                }
                case PARTICIPANTS_SELECTION -> updateParticipants(meeting, Long.valueOf(callback));
                case SUBJECT_SELECTION -> {
                    updateSubject(meeting, callback);
                    setNextState(userId);
                }
                case SUBJECT_DURATION_SELECTION -> {
                    updateTimeDiscussion(meeting, callback);
                    setNextState(userId);
                }
                case QUESTION_SELECTION -> updateQuestion(meeting, callback);
                case DATE_SELECTION -> updateDate(meeting, callback);
                case TIME_SELECTION -> updateTime(meeting, callback);
                case ADDRESS_SELECTION -> {
                    updateAddress(meeting, callback);
                    setNextState(userId);
                }
            }
        } else {
            meeting.setState(MeetingState.GROUP_SELECTION);
        }
        meetingCacheManager.saveData(userId, meeting);
    }

    private void updateTimeDiscussion(Meeting meeting, String callback) {
        Subject subject = meeting.getSubject();
        subject.setDuration(Integer.parseInt(callback));
    }

    public void updateState(long userId, MeetingState state) {
        Meeting meeting = getByOwnerId(userId);
        meeting.setState(state);
    }

    public void updateGroup(Meeting meeting, Long groupId) {
        log.info("updating group {} in meeting", groupId);
        Group group = meeting.getGroup();
        group.setId(groupId);
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

    public void updateSubject(Meeting meeting, String subject) {
        log.info("updating subject '{}' in meeting", subject);
        Subject oldSubject = meeting.getSubject();
        oldSubject.setTitle(subject);
    }

    public void updateQuestion(Meeting meeting, String question) {
        Subject subject = meeting.getSubject();
        List<Question> questions = subject.getQuestions();
        questions.add(Question.builder().title(question).build());
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

        if (!meetingDate.isEmpty()){
            MeetingDate date = meetingDate.get();
            List<MeetingTime> times = date.getTime();
            Optional<MeetingTime> meetingTime = times.stream().filter((time) -> LocalDateTime.of(date.getDate(), time.getTime()).isEqual(localDateTime)).findFirst();
            if (!meetingTime.isEmpty()){
                times.remove(meetingTime.get());
            }else {
                MeetingTime time = MeetingTime.builder().date(date).time(localDateTime.toLocalTime()).build();
                times.add(time);
                date.setTime(times);
            }
        }
    }

    public void updateAddress(Meeting meeting, String address) {
        meeting.setAddress(address);
    }

    public void removeByOwnerId(long ownerId) {
        //TODO возможность удаления из базы
        meetingCacheManager.clearData(ownerId);
    }
}
