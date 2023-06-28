package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.exceptions.UserNotFoundException;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.ufanet.meetingsbot.constants.ToggleButton.NEXT;
import static com.ufanet.meetingsbot.constants.ToggleButton.PREV;

@Component
@RequiredArgsConstructor
public class MeetingConstructor {
    private final AccountService accountService;
    private final GroupService groupService;

    public MeetingDto mapToDto(Meeting meeting) {
        AccountDto accountDto = accountService.mapToDto(meeting.getOwner());
        Long meetingId = meeting.getId();

        Set<AccountDto> participants =
                meeting.getParticipants().stream().map(accountService::mapToDto).collect(Collectors.toSet());
        Map<LocalDate, Set<ZonedDateTime>> dates = meeting.getDates().stream()
                .collect(Collectors.toMap(MeetingDate::getDate,
                        t -> t.getMeetingTimes().stream()
                                .map(MeetingTime::getDateTime)
                                .collect(Collectors.toSet())));

        Long groupId = meeting.getGroup().getId();
        MeetingState state = meeting.getState();
        String title = meeting.getSubject().getTitle();
        Set<String> questions = meeting.getSubject().getQuestions();
        Integer duration = meeting.getSubject().getDuration();
        return MeetingDto.builder()
                .id(meetingId).owner(accountDto)
                .participants(participants)
                .datesMap(dates).groupId(groupId)
                .subjectTitle(title)
                .subjectDuration(duration)
                .state(state).questions(questions)
                .build();
    }

    public Meeting mapToEntity(MeetingDto meetingDto) {
        Account owner = accountService.getByUserId(meetingDto.getOwner().getId())
                .orElseThrow(UserNotFoundException::new);
        Group group = groupService.getById(meetingDto.getGroupId()).orElseThrow();
        Map<LocalDate, Set<ZonedDateTime>> datesMap = meetingDto.getDatesMap();

        Meeting meeting = new Meeting();
        Set<MeetingDate> dates = new HashSet<>();
        for (Map.Entry<LocalDate, Set<ZonedDateTime>> entry : datesMap.entrySet()) {
            LocalDate localDate = entry.getKey();
            MeetingDate meetingDate = MeetingDate.builder().date(localDate).meeting(meeting).build();
            Set<ZonedDateTime> zonedDateTimes = entry.getValue();
            Set<MeetingTime> meetingTimes = zonedDateTimes.stream()
                    .map((dt) -> MeetingTime.builder().meetingDate(meetingDate).dateTime(dt).build())
                    .collect(Collectors.toSet());
            meetingDate.setMeetingTimes(meetingTimes);
            dates.add(meetingDate);
        }
        Set<Account> accounts = meetingDto.getParticipants().stream().map(accountService::mapToEntity)
                .collect(Collectors.toSet());

        Set<AccountMeeting> accountMeetings = new HashSet<>();
        for (Account member : accounts) {
            AccountMeeting accountMeeting = AccountMeeting.builder().meeting(meeting).account(member).build();
            accountMeetings.add(accountMeeting);
        }
        Subject subject = Subject.builder().title(meetingDto.getSubjectTitle())
                .duration(meetingDto.getSubjectDuration())
                .questions(meetingDto.getQuestions()).build();

        meeting.setSubject(subject);
        meeting.setAccountMeetings(accountMeetings);
        meeting.setState(meetingDto.getState());
        meeting.setUpdatedDt(meetingDto.getUpdatedDt());
        meeting.setUpdatedDt(meetingDto.getCreatedDt());
        meeting.setAddress(meetingDto.getAddress());
        meeting.setOwner(owner);
        meeting.setGroup(group);
        meeting.setDates(dates);
        return meeting;
    }

    public MeetingDto mapIfPresentOrElseGet(Optional<Meeting> meeting,
                                            Supplier<? extends MeetingDto> supplier) {
        if (meeting.isPresent()) {
            return this.mapToDto(meeting.get());
        } else {
            return supplier.get();
        }
    }

    public <X extends Throwable> MeetingDto mapIfPresentOrElseThrow(Optional<Meeting> meeting,
                                                                    Supplier<? extends X> exceptionSupplier) throws X{
        if (meeting.isPresent()) {
            return this.mapToDto(meeting.get());
        } else {
            throw exceptionSupplier.get();
        }
    }


    public void updateParticipants(MeetingDto meetingDto, long participantId) {
        Set<AccountDto> participantIds = meetingDto.getParticipants();
        long groupId = meetingDto.getGroupId();
        Set<Account> accounts = accountService.getAccountByGroupsIdAndIdNot(groupId, meetingDto.getOwner().getId());
        boolean removed = participantIds.removeIf(t -> t.getId() == participantId);
        if (!removed) {
            AccountDto accountDto = accounts.stream()
                    .filter(account -> account.getId() == participantId)
                    .map(accountService::mapToDto).findFirst().orElseThrow();
            participantIds.add(accountDto);
        }
        meetingDto.setParticipants(participantIds);
    }

    public void updateQuestion(MeetingDto meetingDto, String question) {
        Set<String> questions = meetingDto.getQuestions();
        if (questions.contains(question)) {
            questions.remove(question);
        } else questions.add(question);
        meetingDto.setQuestions(questions);
    }

    public void updateDate(MeetingDto meetingDto, String callback) {
        Map<LocalDate, Set<ZonedDateTime>> datesMap = meetingDto.getDatesMap();
        if (!callback.startsWith(NEXT.name()) && !callback.startsWith(PREV.name())) {
            LocalDate localDate = LocalDate.parse(callback);
            if (datesMap.containsKey(localDate)) {
                datesMap.remove(localDate);
            } else datesMap.put(localDate, new TreeSet<>());
        }
        meetingDto.setDatesMap(datesMap);
    }

    public void updateTime(MeetingDto meetingDto, String callback) {
        ZonedDateTime znd = ZonedDateTime.parse(callback);
        LocalDate localDate = znd.toLocalDate();
        Map<LocalDate, Set<ZonedDateTime>> datesMap = meetingDto.getDatesMap();
        Set<ZonedDateTime> zonedDateTimes = datesMap.get(localDate);
        if (zonedDateTimes.contains(znd)) {
            zonedDateTimes.remove(znd);
        } else zonedDateTimes.add(znd);
        meetingDto.setDatesMap(datesMap);
    }
}
