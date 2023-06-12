package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import com.ufanet.meetingsbot.state.MeetingState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@CacheConfig(cacheNames = {"meeting"})
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final AccountService accountService;
    @Transactional
    @CacheEvict(key = "#meeting.owner.id", value = "meeting")
    public void save(Meeting meeting) {
        log.info("saving meeting by user {}", meeting.getOwner().getId());
        meetingRepository.save(meeting);
    }
    @CachePut(key = "#meeting.owner.id", value = "meeting")
    public void update(long id, Meeting meeting){

    }

    @Cacheable(key = "#ownerId", value = "meeting")
    public Meeting getMeetingByOwnerId(long ownerId) {
        Optional<Meeting> meeting = meetingRepository.findByOwnerId(ownerId);
        if (meeting.isEmpty()){
            Account account = accountService.getById(ownerId).orElseThrow();
            return Meeting.builder().owner(account).build();
        }
        return meeting.get();
    }
    @CachePut(key = "#meeting.owner.id", value = "meeting")
    public void setNextState(long userId) {
        Meeting meeting = getMeetingByOwnerId(userId);
        MeetingState currState = meeting.getState();
        MeetingState[] values = MeetingState.values();
        int current = currState.ordinal();
        MeetingState nextState = values[current + 1];
        meeting.setState(nextState);
    }

    @CachePut(key = "#meeting.owner.id", value = "meeting")
    public void update(long userId, String callback) {
        Meeting meeting = getMeetingByOwnerId(userId);
        MeetingState state = meeting.getState();
        switch (state) {
            case GROUP_SELECTION -> updateGroup(meeting, Long.valueOf(callback));
            case PARTICIPANTS_SELECTION -> updateParticipants(meeting, Long.valueOf(callback));
            case SUBJECT_SELECTION -> updateSubject(meeting, callback);
            case QUESTION_SELECTION -> updateQuestion(meeting, callback);
            case DATE_SELECTION -> updateDate(meeting, callback);
            case TIME_SELECTION -> updateTime(meeting, callback);
            case ADDRESS_SELECTION -> updateAddress(meeting, callback);
        }
    }
    @CachePut(key = "#meeting.owner.id", value = "meeting")
    public void updateState(long userId, MeetingState state){
        Meeting meeting = getMeetingByOwnerId(userId);
        meeting.setState(state);
    }

    public void updateGroup(Meeting meeting, Long groupId) {
        log.info("updating group {} in meeting", groupId);
        meeting.setState(MeetingState.GROUP_SELECTION);
        Group group = meeting.getGroup();
        group.setId(groupId);
    }

    public void updateParticipants(Meeting meeting, Long participantId) {
        log.info("updating participant {} in meeting", participantId);
        meeting.setState(MeetingState.PARTICIPANTS_SELECTION);
        List<Account> participants = meeting.getAccounts();
        Optional<Account> participant = participants.stream()
                .filter(p -> p.getId() == participantId).findFirst();
        if (participant.isEmpty()) {
        }
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
    }

    public void updateDate(Meeting meeting, String date) {

    }

    public void updateTime(Meeting meeting, String time) {

    }

    public void updateAddress(Meeting meeting, String address) {
        meeting.setAddress(address);
    }
    @Transactional
    @CacheEvict(key = "#ownerId")
    public void removeByOwnerId(long ownerId) {
        Meeting meeting = getMeetingByOwnerId(ownerId);
        meetingRepository.delete(meeting);
    }
}
