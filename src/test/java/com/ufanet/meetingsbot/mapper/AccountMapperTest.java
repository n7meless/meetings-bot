package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.entity.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.User;

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
        AccountDto dto = AccountMapper.MAPPER.mapWithSettings(entity);

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
    @Test
    void shouldReturnAccountEntityWhenMapsFromTelegramUser() {
        //given
        User user = new User();
        user.setId(1L);
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setUserName("username");

        //when
        Account entity = AccountMapper.MAPPER.mapToEntityFromTgUser(user);

        //then
        Assertions.assertEquals(user.getId(), entity.getId());
        Assertions.assertEquals(user.getUserName(), entity.getUsername());
        Assertions.assertEquals(user.getFirstName(), entity.getFirstname());
        Assertions.assertEquals(user.getLastName(), entity.getLastname());
    }
    @Test
    void shouldReturnAccountDtoWhenMapsFromTelegramUser() {
        //given
        User user = new User();
        user.setId(1L);
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setUserName("username");

        //when
        AccountDto dto = AccountMapper.MAPPER.mapToDtoFromTgUser(user);

        //then
        Assertions.assertEquals(user.getId(), dto.getId());
        Assertions.assertEquals(user.getUserName(), dto.getUsername());
        Assertions.assertEquals(user.getFirstName(), dto.getFirstname());
        Assertions.assertEquals(user.getLastName(), dto.getLastname());
    }
}
