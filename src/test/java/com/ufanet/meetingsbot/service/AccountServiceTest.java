package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.container.AbstractTestcontainersDB;
import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AccountServiceTest extends AbstractTestcontainersDB {

    @MockBean
    private AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;

    @Test
    void shouldReturnCreatedUserWhenSave() {
        //given
        Account account = Account.builder().id(1L).firstname("firstname")
                .lastname("lastname").username("username")
                .createdDt(LocalDateTime.now()).build();

        //when
        Mockito.when(accountRepository.save(Mockito.any(Account.class))).thenReturn(account);
        Account created = accountService.save(account);

        //then
        assertEquals(created.getId(), account.getId());
        assertEquals(created.getFirstname(), account.getFirstname());
        assertEquals(created.getLastname(), account.getLastname());
        assertEquals(created.getUsername(), account.getUsername());
    }
    @Test
    void shouldReturnAccountWhenSaveTelegramUser() {
        //given
        User user = new User();
        user.setId(1L);
        user.setUserName("username");
        user.setFirstName("firstname");
        user.setLastName("lastname");

        Account account = Account.builder().id(user.getId())
                .username(user.getUserName())
                .lastname(user.getLastName())
                .firstname(user.getFirstName()).build();
        //when
        Mockito.when(accountRepository.save(Mockito.any(Account.class)))
                .thenReturn(account);

        Account created = accountService.saveTgUser(user);

        //then
        assertEquals(created.getId(), user.getId());
        assertEquals(created.getFirstname(), user.getFirstName());
        assertEquals(created.getLastname(), user.getLastName());
        assertEquals(created.getUsername(), user.getUserName());
    }

}
