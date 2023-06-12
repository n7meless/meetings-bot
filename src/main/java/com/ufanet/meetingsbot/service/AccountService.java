package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Settings;
import com.ufanet.meetingsbot.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Service
@CacheConfig(cacheNames = "user")
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    @Cacheable(key = "#userId")
    public Optional<Account> getById(long userId) {
        return accountRepository.findById(userId);
    }

    @CachePut(key = "#userId", value = "user")
    public void update(long userId, Account newAccount) {
        Account account = getById(userId).orElseThrow();
        account.setGroups(newAccount.getGroups());
        account.setFirstname(newAccount.getFirstname());
        account.setLastname(newAccount.getLastname());
        account.setUsername(newAccount.getUsername());
        account.setMeetings(newAccount.getMeetings());
        account.setSettings(newAccount.getSettings());
    }
    @CachePut(key = "#account.id", value = "account")
    public void save(Account account) {
        accountRepository.save(account);
    }

    public void saveTgUser(User user) {
        Account account = Account.builder().id(user.getId())
                .firstname(user.getFirstName())
                .lastname(user.getLastName())
                .username(user.getUserName())
                .build();
        Settings settings = Settings.builder().account(account).build();
        account.setSettings(settings);
        save(account);
    }

}
