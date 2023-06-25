package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.MeetingStateCache;
import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import com.ufanet.meetingsbot.repository.MeetingTimeRepository;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
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
    private @Value("${telegram.bot.timeZone}") String appTimeZone;

    public List<Meeting> getMeetingsByOwnerIdOrParticipantsId(long userId) {
        return meetingRepository.findMeetingsByAccountMeetings_AccountIdOrOwnerIdEquals(userId, userId);
    }

    @Transactional
    public Meeting save(Meeting meeting) {
        return meetingRepository.save(meeting);
    }

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
                time.getTime();
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
        save(meeting);
        meetingStateCache.evict(owner.getId());
    }

    @Transactional
    public Meeting getByOwnerIdAndStateNotReady(long ownerId) {
        Meeting meetingCache = meetingStateCache.get(ownerId);
        if (meetingCache == null) {
            Meeting meeting = meetingRepository.findByOwnerIdAndStateIsNotIn(ownerId,
                            List.of(MeetingState.CONFIRMED, MeetingState.AWAITING, MeetingState.CANCELED))
                    .orElseGet(() -> createMeeting(ownerId));
            meetingStateCache.save(ownerId, meeting);
            return meeting;
        }
        return meetingCache;
    }

    public Meeting getByOwnerId(long ownerId) {
        Meeting meetingCache = meetingStateCache.get(ownerId);
        if (meetingCache == null) {
            Optional<Meeting> optionalMeeting = meetingRepository.findByOwnerId(ownerId);
            if (optionalMeeting.isPresent()) {
                return optionalMeeting.get();
            } else {
                Meeting meeting = new Meeting();
                Account account = accountService.getByUserId(ownerId).orElseThrow();
                meeting.setOwner(account);
                meetingStateCache.save(ownerId, meeting);
                return meeting;
            }
        }
        return meetingCache;
    }

    public Meeting createMeeting(long ownerId) {
        Account account = accountService.getByUserId(ownerId).orElseThrow();
        Meeting meeting = new Meeting();
        meeting.setCreatedDt(LocalDateTime.now());
        meeting.setUpdatedDt(LocalDateTime.now());
        meeting.setOwner(account);
//        meeting.setAccountMeetings(new ArrayList<>());
//        meeting.setParticipants(new ArrayList<>());
//        meeting.setDates(new ArrayList<>());
        meeting.setState(MeetingState.GROUP_SELECT);
        meetingStateCache.save(ownerId, meeting);
        return meeting;
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
        Optional<Account> participantOptional = accountService.getByUserId(participantId);
        Set<AccountMeeting> participants = meeting.getAccountMeetings();

        if (participantOptional.isPresent()) {
            Account participant = participantOptional.get();
            boolean match = participants.stream().anyMatch(t -> t.getAccount().getId() == participantId);
            if (match) {
                participants.removeIf(t -> t.getAccount().getId() == participantId);
            } else participants.add(AccountMeeting.builder().meeting(meeting).account(participant).build());
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
        save(meeting);
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

    public void updateSubject(Meeting meeting, String callback) {
        Subject subject = meeting.getSubject();
        if (subject == null) {
            subject = new Subject();
        }
        subject.setTitle(callback);
        subject.setMeeting(meeting);
        meeting.setSubject(subject);
    }

    public void updateQuestion(Meeting meeting, String question) {
        Subject subject = meeting.getSubject();
        Set<Question> questions = subject.getQuestions();
        Optional<Question> optional = questions.stream()
                .filter((q) -> q.getTitle().equals(question)).findFirst();

        if (optional.isEmpty()) {
            Question newQuestion = Question.builder().title(question)
                    .subject(subject).build();
            questions.add(newQuestion);
        } else questions.remove(optional.get());
        subject.setQuestions(questions);
        meeting.setSubject(subject);
    }

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

    @SneakyThrows(value = IllegalArgumentException.class)
    public void updateTime(Meeting meeting, String callback) {
        Set<MeetingDate> meetingDates = meeting.getDates();

        LocalDateTime localDateTime = LocalDateTime.parse(callback, CustomFormatter.DATE_TIME_FORMATTER);

        LocalDate localDate = localDateTime.toLocalDate();
        MeetingDate date = meetingDates.stream().filter(meetingDate -> meetingDate.getDate()
                .isEqual(localDate)).findFirst().orElseThrow();

        Set<MeetingTime> times = date.getMeetingTimes();
        Optional<MeetingTime> meetingTime = times.stream()
                .filter(t -> t.getTime().isEqual(localDateTime)).findFirst();

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
    }

    @Transactional
    public void deleteByOwnerId(long ownerId) {
        meetingRepository.deleteByOwnerId(ownerId);
        meetingStateCache.evict(ownerId);
    }

    public void deleteById(long meetingId) {
        meetingRepository.deleteById(meetingId);
    }

    @Transactional
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

    @Transactional
    public Optional<MeetingTime> getByMeetingIdAndConfirmedState(long meetingId) {
        return meetingTimeRepository.findByMeetingIdAndConfirmed(meetingId);
    }

    public void processConfirmedMeeting(long userId, Meeting meeting, Optional<MeetingTime> confirmed) {
        MeetingTime meetingTime = confirmed.get();
        MeetingDate meetingDate = meetingTime.getMeetingDate();
//        meetingDate.setMeetingTimes(Set.of(meetingTime));
//        meeting.setDates(Set.of(meetingDate));
//        meeting.setState(MeetingState.CONFIRMED);
//
        meeting.removeDateIf(date -> !date.equals(meetingDate));
        meetingDate.removeTimeIf(time -> !time.equals(meetingTime));
        meetingStateCache.evict(userId);
        save(meeting);
    }

    @Transactional
    public List<Meeting> getMeetingsByUserIdAndState(long userId, MeetingState state) {
        return meetingRepository.findByAccountMeetingsIdOrOwnerIdAndStateEquals(userId, userId, state);
    }

    public void setAccountTimeState(long userId, Meeting meeting, AccountTime accountTime, Status status) {
//        MeetingTime meetingTime = accountTime.getMeetingTime();
//        MeetingDate meetingDate = meetingTime.getMeetingDate();
        accountTime.setStatus(status);
//
//        meetingTime.addAccountTime(accountTime);
//        meetingDate.addMeetingTime(meetingTime);
//        meeting.addMeetingDate(meetingDate);

        accountService.saveAccountTime(accountTime);
//        meetingStateCache.save(userId, meeting);
        meetingStateCache.evict(userId);
    }
}
