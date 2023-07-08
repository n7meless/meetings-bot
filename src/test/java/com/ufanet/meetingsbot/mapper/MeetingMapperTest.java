package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.*;
import com.ufanet.meetingsbot.entity.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MeetingMapperTest {

    @Test
    void shouldReturnMeetingDtoWhenMapsFromEntity() {
        //given
        Meeting entity = Meeting.builder()
                .id(1L)
                .updatedDt(LocalDateTime.now())
                .createdDt(LocalDateTime.now())
                .state(MeetingState.GROUP_SELECT)
                .address("address")
                .build();

        //when
        MeetingDto dto = MeetingMapper.MAPPER.map(entity);

        //then
        assertNotNull(dto);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getAddress(), entity.getAddress());
        assertEquals(dto.getCreatedDt(), entity.getCreatedDt());
        assertEquals(dto.getUpdatedDt(), entity.getUpdatedDt());
        assertEquals(dto.getState(), entity.getState());
    }

    @Test
    void shouldReturnMeetingWhenMapsFromEntity() {
        //given
        Meeting dto = Meeting.builder()
                .id(1L)
                .updatedDt(LocalDateTime.now())
                .createdDt(LocalDateTime.now())
                .state(MeetingState.GROUP_SELECT)
                .address("address")
                .build();

        //when
        MeetingDto entity = MeetingMapper.MAPPER.map(dto);

        //then
        assertNotNull(dto);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getAddress(), entity.getAddress());
        assertEquals(dto.getCreatedDt(), entity.getCreatedDt());
        assertEquals(dto.getUpdatedDt(), entity.getUpdatedDt());
        assertEquals(dto.getState(), entity.getState());
    }

    @Test
    void shouldReturnFullDtoWhenMapsFromEntity() {
        //given
        Account owner = Account.builder()
                .id(10L).createdDt(LocalDateTime.now())
                .username("username").firstname("firstname")
                .lastname("lastname")
                .build();

        Set<Account> participants = new HashSet<>();
        for (long i = 1; i <= 5; i++) {
            Account account = Account.builder()
                    .id(i).username("username")
                    .firstname("firstname")
                    .lastname("lastname")
                    .build();
            participants.add(account);
        }
        Set<MeetingDate> meetingDates = new HashSet<>();
        for (long i = 1; i <= 5; i++) {
            MeetingDate meetingDate = new MeetingDate();

            Set<MeetingTime> meetingTimes = new HashSet<>();
            for (long j = 1; j <= 5; j++) {
                MeetingTime meetingTime = MeetingTime.builder().meetingDate(meetingDate)
                        .dateTime(ZonedDateTime.now()).id(i).build();

                Set<AccountTime> accountTimes = new HashSet<>();
                for (Account participant : participants) {
                    AccountTime accountTime = AccountTime.builder()
                            .status(Status.AWAITING)
                            .account(participant)
                            .meetingTime(meetingTime)
                            .build();

                    accountTimes.add(accountTime);
                }
                meetingTime.setAccountTimes(accountTimes);
                meetingTimes.add(meetingTime);
            }
            meetingDate.setDate(LocalDate.now());
            meetingDate.setMeetingTimes(meetingTimes);
            meetingDates.add(meetingDate);
        }

        Meeting entity = Meeting.builder()
                .id(1L)
                .owner(owner)
                .participants(participants)
                .dates(meetingDates)
                .updatedDt(LocalDateTime.now())
                .createdDt(LocalDateTime.now())
                .state(MeetingState.GROUP_SELECT)
                .address("address")
                .build();

        //when
        MeetingDto dto = MeetingMapper.MAPPER.mapToFullDto(entity);

        //then
        assertNotNull(dto);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getAddress(), entity.getAddress());
        assertEquals(dto.getCreatedDt(), entity.getCreatedDt());
        assertEquals(dto.getUpdatedDt(), entity.getUpdatedDt());
        assertEquals(dto.getState(), entity.getState());
        assertNotNull(dto.getDates());
        assertEquals(dto.getDates().size(), entity.getDates().size());
        assertNotNull(dto.getParticipants());
        assertEquals(dto.getParticipants().size(), entity.getParticipants().size());

    }

    @Test
    void shouldReturnFullEntityWhenMapsFromDto() {
        //given
        AccountDto owner = AccountDto.builder()
                .id(10L).username("username")
                .firstname("firstname")
                .lastname("lastname")
                .build();

        Set<AccountDto> participants = new HashSet<>();
        for (long i = 1; i <= 5; i++) {
            AccountDto account = AccountDto.builder()
                    .id(i).username("username")
                    .firstname("firstname")
                    .lastname("lastname")
                    .build();
            participants.add(account);
        }
        Set<MeetingDateDto> meetingDates = new HashSet<>();
        for (long i = 1; i <= 5; i++) {
            MeetingDateDto meetingDate = new MeetingDateDto();

            Set<MeetingTimeDto> meetingTimes = new HashSet<>();
            for (long j = 1; j <= 5; j++) {
                MeetingTimeDto meetingTime = MeetingTimeDto.builder().meetingDate(meetingDate)
                        .dateTime(ZonedDateTime.now()).id(i).build();

                List<AccountTimeDto> accountTimes = new ArrayList<>();
                for (AccountDto participant : participants) {
                    AccountTimeDto accountTime = AccountTimeDto.builder()
                            .status(Status.AWAITING)
                            .account(participant)
                            .meetingTime(meetingTime)
                            .build();

                    accountTimes.add(accountTime);
                }
                meetingTime.setAccountTimes(accountTimes);
                meetingTimes.add(meetingTime);
            }
            meetingDate.setDate(LocalDate.now());
            meetingDate.setMeetingTimes(meetingTimes);
            meetingDates.add(meetingDate);
        }

        MeetingDto dto = MeetingDto.builder()
                .id(1L)
                .owner(owner)
                .participants(participants)
                .dates(meetingDates)
                .updatedDt(LocalDateTime.now())
                .createdDt(LocalDateTime.now())
                .state(MeetingState.GROUP_SELECT)
                .address("address")
                .build();

        //when
        Meeting entity = MeetingMapper.MAPPER.mapToFullEntity(dto);

        //then
        assertNotNull(entity);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getAddress(), dto.getAddress());
        assertEquals(entity.getCreatedDt(), dto.getCreatedDt());
        assertEquals(entity.getUpdatedDt(), dto.getUpdatedDt());
        assertEquals(entity.getState(), dto.getState());
        assertNotNull(entity.getDates());
        assertEquals(entity.getDates().size(), dto.getDates().size());
        assertNotNull(entity.getParticipants());
        assertEquals(entity.getParticipants().size(), dto.getParticipants().size());
    }
}
