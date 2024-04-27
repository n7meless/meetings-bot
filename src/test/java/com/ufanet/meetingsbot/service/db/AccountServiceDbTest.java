package com.ufanet.meetingsbot.service.db;

import com.ufanet.meetingsbot.annotation.DatabaseTest;
import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.ServiceTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DatabaseTest
@ContextConfiguration(classes = ServiceTestConfiguration.class)
public class AccountServiceDbTest {

    @Autowired
    private AccountService accountService;

    @Test
    void shouldReturnAccount_whenSaveInDatabase() {
        Account account = Account.builder().id(123L)
                .firstname("Andrey").lastname("Makarov")
                .username("makarov999")
                .build();

        Account saved = accountService.save(account);

        assertNotNull(saved);
        assertEquals(account.getId(), saved.getId());
        assertEquals(account.getFirstname(), saved.getFirstname());
        assertEquals(account.getLastname(), saved.getLastname());
        assertEquals(account.getUsername(), saved.getUsername());
    }

    @Test
    void shouldReturnAccount_whenGetFromDatabase() {
        Optional<Account> account = accountService.find(1L);

        assertTrue(account.isPresent());
        assertNotNull(account.get().getId());
        assertNotNull(account.get().getFirstname());
        assertNotNull(account.get().getLastname());
        assertNotNull(account.get().getUsername());
    }

    @Test
    void shouldUpdateAccountFromTgUser_whenGetFromDatabaseAndUpdate() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Ivan");
        user.setLastName("Ivanov");
        user.setUserName("ivanov123");

        Optional<Account> accountOptional = accountService.find(user.getId());
        assertTrue(accountOptional.isPresent());

        Account oldAcc = accountOptional.get();
        assertNotEquals(oldAcc.getFirstname(), user.getFirstName());
        assertNotEquals(oldAcc.getLastname(), user.getLastName());
        assertNotEquals(oldAcc.getUsername(), user.getUserName());


        Account updatedAcc = accountService.updateFromTgUser(oldAcc, user);

        assertNotNull(updatedAcc);
        assertEquals(user.getId(), updatedAcc.getId());
        assertEquals(user.getFirstName(), updatedAcc.getFirstname());
        assertEquals(user.getLastName(), updatedAcc.getLastname());
        assertEquals(user.getUserName(), updatedAcc.getUsername());
    }

    @Test
    void shouldReturnAccount_whenCreateFromTgUser() {
        User user = new User();
        user.setId(123L);
        user.setFirstName("Alexey");
        user.setLastName("Fedorov");
        user.setUserName("alex_fedorov");

        Account account = accountService.create(user);

        assertNotNull(account);
        assertNotNull(account.getId());
        assertEquals(user.getId(), account.getId());
        assertEquals(user.getFirstName(), account.getFirstname());
        assertEquals(user.getLastName(), account.getLastname());
        assertEquals(user.getUserName(), account.getUsername());
    }
}
