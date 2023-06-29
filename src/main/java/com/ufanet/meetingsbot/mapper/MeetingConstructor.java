package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.*;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.ufanet.meetingsbot.constants.ToggleButton.NEXT;
import static com.ufanet.meetingsbot.constants.ToggleButton.PREV;

@Component
@RequiredArgsConstructor
public class MeetingConstructor {
    private final AccountMapper accountMapper;
    private final GroupService groupService;
    private final AccountMeetingMapper accountMeetingMapper;
    private final SubjectMapper subjectMapper;

    public MeetingDto create(Account account) {
        AccountDto accountDto = accountMapper.map(account);
        MeetingDto meetingDto = MeetingDto.builder().owner(accountDto)
                .createdDt(ZonedDateTime.now())
                .updatedDt(ZonedDateTime.now())
                .dates(new HashSet<>())
                .state(MeetingState.GROUP_SELECT).build();

        meetingDto.addParticipant(accountDto);
        return meetingDto;
    }

    public MeetingDto mapToDto(Meeting meeting) {

        AccountDto owner = accountMapper.map(meeting.getOwner());
        Set<MeetingDate> dates = meeting.getDates();
        Set<AccountMeeting> accountMeetings = meeting.getAccountMeetings();
        Set<AccountMeetingDto> accountMeetingDtos = accountMeetings.stream()
                .map(accountMeetingMapper::map).collect(Collectors.toSet());

        Set<MeetingDateDto> meetingDateDtos = new HashSet<>();
        for (MeetingDate date : dates) {

            MeetingDateDto meetingDateDto = MeetingDateDto.builder()
                    .date(date.getDate())
                    .id(date.getId())
                    .build();

            Set<MeetingTime> times = date.getMeetingTimes();
            Set<MeetingTimeDto> timeDtos = new HashSet<>();

            for (MeetingTime time : times) {
                MeetingTimeDto meetingTimeDto = MeetingTimeDto.builder()
                        .dateTime(time.getDateTime())
                        .id(time.getId())
                        .build();

                Set<AccountTime> accountTimes = time.getAccountTimes();
                Set<AccountTimeDto> accountTimeDtos = new HashSet<>();
                for (AccountTime accountTime : accountTimes) {
                    Account account = accountTime.getAccount();
                    AccountDto accountDto = accountMapper.map(account);

                    AccountTimeDto build = AccountTimeDto.builder()
                            .id(accountTime.getId())
                            .meetingTime(meetingTimeDto)
                            .account(accountDto)
                            .status(accountTime.getStatus())
                            .build();
                    accountTimeDtos.add(build);
                }
                meetingTimeDto.setAccountTimes(accountTimeDtos);
                timeDtos.add(meetingTimeDto);
            }
            meetingDateDto.setMeetingTimes(timeDtos);
            meetingDateDtos.add(meetingDateDto);
        }

        SubjectDto subjectDto = subjectMapper.map(meeting.getSubject());
        return MeetingDto.builder()
                .id(meeting.getId())
                .groupId(meeting.getGroup().getId())
                .state(meeting.getState())
                .dates(meetingDateDtos)
                .owner(owner)
                .createdDt(meeting.getCreatedDt())
                .updatedDt(meeting.getUpdatedDt())
                .address(meeting.getAddress())
                .accountMeetings(accountMeetingDtos)
                .subjectDto(subjectDto)
                .build();
    }

    public Meeting mapToEntity(MeetingDto meetingDto) {
        Group group = groupService.getById(meetingDto.getGroupId()).orElseThrow();

        AccountDto owner = meetingDto.getOwner();
        Account account = accountMapper.map(owner);

        Meeting meeting = Meeting.builder().group(group)
                .id(meetingDto.getId())
                .createdDt(meetingDto.getCreatedDt())
                .updatedDt(meetingDto.getUpdatedDt())
                .owner(account)
                .address(meetingDto.getAddress())
                .state(meetingDto.getState())
                .build();

        SubjectDto subjectDto = meetingDto.getSubjectDto();
        Subject subject = subjectMapper.map(subjectDto);
        if (subject != null) subject.setMeeting(meeting);
        meeting.setSubject(subject);

        Set<AccountMeetingDto> accountMeetingsDto = meetingDto.getAccountMeetings();
        Set<AccountMeeting> accountMeetings = new HashSet<>();
        for (AccountMeetingDto accountMeetingDto : accountMeetingsDto) {
            AccountMeeting accountMeeting = accountMeetingMapper.map(accountMeetingDto);
            accountMeeting.setMeeting(meeting);

            AccountDto accountDto = accountMeetingDto.getAccount();
            Account mapped = accountMapper.map(accountDto);
            accountMeeting.setAccount(mapped);
            accountMeetings.add(accountMeeting);
        }
        meeting.setAccountMeetings(accountMeetings);

        Set<MeetingDateDto> dtoDates = meetingDto.getDates();
        Set<MeetingDate> dates = new HashSet<>();
        for (MeetingDateDto dateDto : dtoDates) {
            MeetingDate meetingDate = MeetingDate.builder().id(dateDto.getId())
                    .meeting(meeting).date(dateDto.getDate())
                    .build();

            Set<MeetingTimeDto> meetingTimeDtos = dateDto.getMeetingTimes();
            Set<MeetingTime> meetingTimes = new HashSet<>();
            for (MeetingTimeDto meetingTimeDto : meetingTimeDtos) {
                MeetingTime meetingTime = MeetingTime.builder().dateTime(meetingTimeDto.getDateTime())
                        .meetingDate(meetingDate).build();

                Set<AccountTimeDto> accountTimesDtos = meetingTimeDto.getAccountTimes();
                Set<AccountTime> accountTimes = new HashSet<>();

                for (AccountTimeDto timeDto : accountTimesDtos) {
                    AccountTime mapped = new AccountTime();
                    AccountDto accountDto = timeDto.getAccount();
                    Account accountMapped = accountMapper.map(accountDto);

                    mapped.setStatus(timeDto.getStatus());
                    mapped.setId(timeDto.getId());
                    mapped.setMeetingTime(meetingTime);
                    mapped.setAccount(accountMapped);
                    accountTimes.add(mapped);
                }

                meetingTime.setAccountTimes(accountTimes);
                meetingTimes.add(meetingTime);
            }
            meetingDate.setMeetingTimes(meetingTimes);
            dates.add(meetingDate);
        }

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
                                                                    Supplier<? extends X> exceptionSupplier) throws X {
        if (meeting.isPresent()) {
            return this.mapToDto(meeting.get());
        } else {
            throw exceptionSupplier.get();
        }
    }


    public void updateParticipants(MeetingDto meetingDto, long participantId, Set<Account> accounts) {
        Set<AccountMeetingDto> participantIds = meetingDto.getAccountMeetings();
        boolean removed = participantIds.removeIf(t -> t.getAccount().getId() == participantId);
        if (!removed) {
            AccountDto accountDto = accounts.stream()
                    .filter(account -> account.getId() == participantId)
                    .map(accountMapper::map).findFirst().orElseThrow();
            AccountMeetingDto accountMeetingDto = AccountMeetingDto.builder()
                    .account(accountDto).build();
            participantIds.add(accountMeetingDto);
        }
        meetingDto.setAccountMeetings(participantIds);
    }

    public void updateQuestion(MeetingDto meetingDto, String question) {
        SubjectDto subjectDto = meetingDto.getSubjectDto();
        Set<String> questions = subjectDto.getQuestions();
        if (questions.contains(question)) {
            questions.remove(question);
        } else questions.add(question);
        subjectDto.setQuestions(questions);
        meetingDto.setSubjectDto(subjectDto);
    }

    public void updateDate(MeetingDto meetingDto, String callback) {
        Set<MeetingDateDto> datesMap = meetingDto.getDates();
        if (!callback.startsWith(NEXT.name()) && !callback.startsWith(PREV.name())) {
            LocalDate localDate = LocalDate.parse(callback);
            Optional<MeetingDateDto> meetingDateDto =
                    datesMap.stream().filter(dto -> dto.getDate().equals(localDate)).findFirst();
            if (meetingDateDto.isPresent()) {
                datesMap.remove(meetingDateDto.get());
            } else {
                MeetingDateDto build = MeetingDateDto.builder()
                        .date(localDate).build();
                datesMap.add(build);
            }
        }
        meetingDto.setDates(datesMap);
    }

    public void updateTime(MeetingDto meetingDto, String callback) {
        ZonedDateTime znd = ZonedDateTime.parse(callback);
        LocalDate localDate = znd.toLocalDate();
        Set<MeetingDateDto> datesMap = meetingDto.getDates();
        Optional<MeetingDateDto> meetingDateDto = datesMap.stream()
                .filter(dto -> dto.getDate().isEqual(localDate)).findFirst();

        if (meetingDateDto.isPresent()) {
            MeetingDateDto dateDto = meetingDateDto.get();
            Set<MeetingTimeDto> meetingTimes = dateDto.getMeetingTimes();
            boolean removed = meetingTimes.removeIf(mtd -> mtd.getDateTime().isEqual(znd));
            if (!removed) {
                MeetingTimeDto meetingTimeDto = MeetingTimeDto.builder().dateTime(znd).build();
                meetingTimes.add(meetingTimeDto);
            }
            dateDto.setMeetingTimes(meetingTimes);
        }
    }

    public void updateAccountTimes(MeetingDto meetingDto, MeetingTime meetingTime) {
        Set<MeetingDateDto> dates = meetingDto.getDates();
        MeetingDate meetingDate = meetingTime.getMeetingDate();

        dates.removeIf(dto -> !Objects.equals(dto.getId(), meetingDate.getId()));
        MeetingDateDto dateDto = dates.stream().findFirst().orElseThrow();

        Set<MeetingTimeDto> meetingTimes = dateDto.getMeetingTimes();
        meetingTimes.removeIf(dto -> !Objects.equals(dto.getId(), meetingTime.getId()));
        MeetingTimeDto meetingTimeDto = meetingTimes.stream().findFirst().orElseThrow();

        Set<AccountTimeDto> accountTimeDtos = meetingTime.getAccountTimes().stream()
                .map(at -> AccountTimeDto.builder().status(at.getStatus())
                        .account(accountMapper.map(at.getAccount()))
                        .id(at.getId()).meetingTime(meetingTimeDto).build())
                .collect(Collectors.toSet());
        meetingTimeDto.setAccountTimes(accountTimeDtos);
    }
}
