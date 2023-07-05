package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.entity.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class AccountMapperTest {

    @Test
    void shouldReturnAccountDtoWhenMapsFromEntity() {
        //given
        Account entity = Account.builder()
                .id(10L).createdDt(LocalDateTime.now())
                .username("username").firstname("firstname")
                .lastname("lastname")
                .build();

        //when
        AccountDto dto = AccountMapper.MAPPER.map(entity);

        //then
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertEquals(dto.getUsername(), entity.getUsername());
        Assertions.assertEquals(dto.getFirstname(), entity.getFirstname());
        Assertions.assertEquals(dto.getLastname(), entity.getLastname());
    }

    @Test
    void shouldReturnAccountEntityWhenMapsFromDto() {
        //given
        AccountDto dto = AccountDto.builder()
                .id(10L).username("username")
                .firstname("firstname")
                .lastname("lastname")
                .build();

        //when
        Account entity = AccountMapper.MAPPER.map(dto);

        //then
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertEquals(dto.getUsername(), entity.getUsername());
        Assertions.assertEquals(dto.getFirstname(), entity.getFirstname());
        Assertions.assertEquals(dto.getLastname(), entity.getLastname());
    }
}
