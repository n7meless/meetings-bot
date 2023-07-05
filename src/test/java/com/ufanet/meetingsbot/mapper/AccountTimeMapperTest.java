package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.dto.AccountTimeDto;
import com.ufanet.meetingsbot.entity.AccountTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccountTimeMapperTest {

    @Test
    void shouldReturnAccountTimeDtoWhenMapsFromEntity() {
        //given
        AccountTime entity = AccountTime.builder()
                .id(10L).status(Status.AWAITING)
                .build();

        //when
        AccountTimeDto dto = AccountTimeMapper.MAPPER.map(entity);

        //then
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertEquals(dto.getStatus(), entity.getStatus());
    }

    @Test
    void shouldReturnAccountTimeEntityWhenMapsFromDto() {
        //given
        AccountTimeDto dto = AccountTimeDto.builder()
                .id(10L).status(Status.AWAITING)
                .build();

        //when
        AccountTime entity = AccountTimeMapper.MAPPER.map(dto);

        //then
        Assertions.assertEquals(entity.getId(), dto.getId());
        Assertions.assertEquals(entity.getStatus(), dto.getStatus());
    }
}
