package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.*;
import com.ufanet.meetingsbot.entity.*;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MeetingMapper {
    MeetingMapper MAPPER = Mappers.getMapper(MeetingMapper.class);

    default MeetingDto mapToFullDto(Meeting meeting) {

        AccountDto owner = AccountMapper.MAPPER.map(meeting.getOwner());
        Set<AccountDto> participants =
                meeting.getParticipants().stream().map(AccountMapper.MAPPER::map)
                        .collect(Collectors.toSet());

        Set<MeetingDate> dates = meeting.getDates();

        Set<MeetingDateDto> meetingDateDtos = new HashSet<>();
        for (MeetingDate date : dates) {

            MeetingDateDto meetingDateDto = MeetingDateDto.builder()
                    .date(date.getDate())
                    .id(date.getId())
                    .build();

            Set<MeetingTime> times = date.getMeetingTimes();
            Set<MeetingTimeDto> timeDtos = new HashSet<>();

            for (MeetingTime time : times) {
                MeetingTimeDto meetingTimeDto = MeetingTimeMapper.MAPPER.map(time);
                Set<AccountTimeDto> accountTimeDtos = time.getAccountTimes().stream()
                        .map(AccountTimeMapper.MAPPER::map).collect(Collectors.toSet());

                meetingTimeDto.setMeetingDate(meetingDateDto);
                meetingTimeDto.setAccountTimes(accountTimeDtos);
                timeDtos.add(meetingTimeDto);
            }
            meetingDateDto.setMeetingTimes(timeDtos);
            meetingDateDtos.add(meetingDateDto);
        }

        SubjectDto subjectDto = SubjectMapper.MAPPER.map(meeting.getSubject());
        GroupDto groupDto = GroupMapper.MAPPER.map(meeting.getGroup());
        return MeetingDto.builder()
                .id(meeting.getId())
                .groupDto(groupDto)
                .state(meeting.getState())
                .dates(meetingDateDtos)
                .owner(owner)
                .createdDt(meeting.getCreatedDt())
                .updatedDt(meeting.getUpdatedDt())
                .address(meeting.getAddress())
                .participants(participants)
                .subjectDto(subjectDto)
                .build();
    }

    @InheritInverseConfiguration
    default Meeting mapToFullEntity(MeetingDto meetingDto) {
        Group group = GroupMapper.MAPPER.map(meetingDto.getGroupDto());

        AccountDto ownerDto= meetingDto.getOwner();
        Account owner = AccountMapper.MAPPER.map(ownerDto);

        Meeting meeting = Meeting.builder().group(group)
                .id(meetingDto.getId())
                .createdDt(meetingDto.getCreatedDt())
                .updatedDt(meetingDto.getUpdatedDt())
                .owner(owner)
                .address(meetingDto.getAddress())
                .state(meetingDto.getState())
                .build();

        SubjectDto subjectDto = meetingDto.getSubjectDto();
        Subject subject = SubjectMapper.MAPPER.map(subjectDto);
        if (subject != null) subject.setMeeting(meeting);
        meeting.setSubject(subject);

        Set<Account> participants =
                meetingDto.getParticipants().stream().map(AccountMapper.MAPPER::map)
                        .collect(Collectors.toSet());
        meeting.setParticipants(participants);

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
                    AccountTime accountTime = AccountTimeMapper.MAPPER.map(timeDto);;

                    Account account = AccountMapper.MAPPER.map(timeDto.getAccount());

                    accountTime.setMeetingTime(meetingTime);
                    accountTime.setAccount(account);
                    accountTimes.add(accountTime);
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
    @Mapping(target = "dates", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "participants", ignore = true)
    MeetingDto map(Meeting entity);
    @InheritInverseConfiguration
    @Mapping(target = "dates", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "participants", ignore = true)
    Meeting map(MeetingDto dto);

    default MeetingDto mapWithMeetingDateAndTimes(Meeting entity) {
        MeetingDto meetingDto = map(entity);
        Set<MeetingDate> dates = entity.getDates();
        Set<MeetingDateDto> dateDtos = new HashSet<>();
        for (MeetingDate date : dates) {
            Set<MeetingTimeDto> meetingTimeDtos = date.getMeetingTimes().stream()
                    .map(MeetingTimeMapper.MAPPER::map).collect(Collectors.toSet());
            MeetingDateDto dateDto = MeetingDateDto.builder().meeting(meetingDto).id(date.getId())
                    .date(date.getDate())
                    .meetingTimes(meetingTimeDtos).build();
            dateDtos.add(dateDto);
        }
        meetingDto.setDates(dateDtos);
        return meetingDto;
    }
    default MeetingDto mapWithMeetingDateAndTimesAndParticipants(Meeting entity) {
        MeetingDto meetingDto = mapWithMeetingDateAndTimes(entity);
        Set<AccountDto> accountDtos = entity.getParticipants().stream().map(AccountMapper.MAPPER::map).collect(Collectors.toSet());
        meetingDto.setParticipants(accountDtos);
        return meetingDto;
    }


    default MeetingDto mapIfPresentOrElseGet(Optional<Meeting> meeting,
                                             Supplier<? extends MeetingDto> supplier) {
        if (meeting.isPresent()) {
            return this.mapToFullDto(meeting.get());
        } else {
            return supplier.get();
        }
    }

    default <X extends Throwable> MeetingDto mapIfPresentOrElseThrow(Optional<Meeting> meeting,
                                                                     Supplier<? extends X> exceptionSupplier) throws X {
        if (meeting.isPresent()) {
            return this.mapToFullDto(meeting.get());
        } else {
            throw exceptionSupplier.get();
        }
    }
}
