package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.MeetingTimeDto;
import com.ufanet.meetingsbot.entity.MeetingTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

public class MeetingTimeMapperTest {

    @Test
    void shouldReturnMeetingTimeDtoWhenMapsFromEntity() {
        //given
        MeetingTime entity = MeetingTime.builder()
                .id(10L).dateTime(ZonedDateTime.now())
                .build();

        //when
        MeetingTimeDto dto = MeetingTimeMapper.MAPPER.map(entity);

        //then
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertTrue(entity.getDateTime().isEqual(dto.getDateTime()));
    }

    @Test
    void shouldReturnMeetingTimeEntityWhenMapsFromDto() {
        //given
        MeetingTime dto = MeetingTime.builder()
                .id(10L).dateTime(ZonedDateTime.now())
                .build();

        //when
        MeetingTimeDto entity = MeetingTimeMapper.MAPPER.map(dto);

        //then
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertTrue(entity.getDateTime().isEqual(dto.getDateTime()));
    }
}
