package com.ufanet.meetingsbot.mapper.impl;

import com.ufanet.meetingsbot.dto.*;
import com.ufanet.meetingsbot.mapper.*;
import com.ufanet.meetingsbot.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingMapperImpl implements MeetingMapper {
    private final AccountMapper accountMapper;
    private final AccountMeetingMapper accountMeetingMapper;
    private final SubjectMapper subjectMapper;
    private final GroupMapper groupMapper;
    private final AccountTimeMapper accountTimeMapper;

    public MeetingDto map(Meeting meeting) {

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

                    AccountTimeDto build = accountTimeMapper.map(accountTime);

                    accountTimeDtos.add(build);
                }
                meetingTimeDto.setAccountTimes(accountTimeDtos);
                timeDtos.add(meetingTimeDto);
            }
            meetingDateDto.setMeetingTimes(timeDtos);
            meetingDateDtos.add(meetingDateDto);
        }

        SubjectDto subjectDto = subjectMapper.map(meeting.getSubject());
        GroupDto groupDto = groupMapper.map(meeting.getGroup());
        return MeetingDto.builder()
                .id(meeting.getId())
                .groupDto(groupDto)
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

    public Meeting map(MeetingDto meetingDto) {
        Group group = groupMapper.map(meetingDto.getGroupDto());

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
                MeetingTime meetingTime = MeetingTime.builder()
                        .dateTime(meetingTimeDto.getDateTime())
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
            return this.map(meeting.get());
        } else {
            return supplier.get();
        }
    }

    public <X extends Throwable> MeetingDto mapIfPresentOrElseThrow(Optional<Meeting> meeting,
                                                                    Supplier<? extends X> exceptionSupplier) throws X {
        if (meeting.isPresent()) {
            return this.map(meeting.get());
        } else {
            throw exceptionSupplier.get();
        }
    }
}
