package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
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

        Account created = accountService.createAccount(user);

        //then
        assertEquals(created.getId(), user.getId());
        assertEquals(created.getFirstname(), user.getFirstName());
        assertEquals(created.getLastname(), user.getLastName());
        assertEquals(created.getUsername(), user.getUserName());
    }

    @Test
    void shouldReturnFindAndReturnOneAccount() {
        //given
        Account expected = Account.builder().firstname("firstname").lastname("lastname").id(1L)
                .createdDt(LocalDateTime.now()).build();

        //when
        Mockito.when(accountRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(expected));
        Optional<Account> created = accountService.getByUserId(1L);

        //then
        assertDoesNotThrow(created::get);
        assertEquals(created.get().getId(), expected.getId());
        assertEquals(created.get().getFirstname(), expected.getFirstname());
        assertEquals(created.get().getUsername(), expected.getUsername());
    }
}