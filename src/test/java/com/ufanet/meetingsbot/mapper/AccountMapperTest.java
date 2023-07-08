package com.ufanet.meetingsbot.mapper;

import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.entity.Settings;
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
        User tgUser = new User();
        tgUser.setId(1L);
        tgUser.setFirstName("firstname");
        tgUser.setLastName("lastname");
        tgUser.setUserName("username");

        //when
        AccountDto dto = AccountMapper.MAPPER.mapToDtoFromTgUser(tgUser);

        //then
        Assertions.assertEquals(tgUser.getId(), dto.getId());
        Assertions.assertEquals(tgUser.getUserName(), dto.getUsername());
        Assertions.assertEquals(tgUser.getFirstName(), dto.getFirstname());
        Assertions.assertEquals(tgUser.getLastName(), dto.getLastname());
    }

    @Test
    void shouldReturnAccountDtoWhenMapsFromEntityWithSettings() {
        //given
        Settings settings = Settings.builder().id(1L).language("ru-RU")
                .timeZone("UTC+03:00").build();

        Account entity = Account.builder()
                .id(10L).createdDt(LocalDateTime.now())
                .username("username").firstname("firstname")
                .lastname("lastname").settings(settings)
                .build();

        //when
        AccountDto dto = AccountMapper.MAPPER.mapWithSettings(entity);

        //then
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertEquals(dto.getUsername(), entity.getUsername());
        Assertions.assertEquals(dto.getFirstname(), entity.getFirstname());
        Assertions.assertEquals(dto.getLastname(), entity.getLastname());
        Assertions.assertEquals(dto.getLanguage(), settings.getLanguage());
        Assertions.assertEquals(dto.getTimeZone(), settings.getTimeZone());
    }


    @Test
    void shouldReturnAccountWhenMapsFromDtoWithSettings() {
        //given
        AccountDto dto = AccountDto.builder()
                .id(10L).timeZone("UTC+03:00").language("ru-RU")
                .username("username").firstname("firstname")
                .lastname("lastname")
                .build();

        //when
        Account entity = AccountMapper.MAPPER.mapWithSettings(dto);
        Settings settings = entity.getSettings();

        //then
        Assertions.assertEquals(dto.getId(), entity.getId());
        Assertions.assertEquals(dto.getUsername(), entity.getUsername());
        Assertions.assertEquals(dto.getFirstname(), entity.getFirstname());
        Assertions.assertEquals(dto.getLastname(), entity.getLastname());
        Assertions.assertEquals(dto.getLanguage(), settings.getLanguage());
        Assertions.assertEquals(dto.getTimeZone(), settings.getTimeZone());
    }
}
