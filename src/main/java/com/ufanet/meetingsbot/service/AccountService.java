package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Settings;
import com.ufanet.meetingsbot.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Service
@CacheConfig(cacheNames = "account")
@EnableCaching
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    @Transactional
    @Cacheable(key = "#userId", value = "account",unless="#result == null")
    public Optional<Account> getByUserId(long userId) {
        return accountRepository.findById(userId);
    }

    @CachePut(key = "#userId", value = "account")
    public void update(long userId, Account newAccount) {
        Account account = getByUserId(userId).orElseThrow();
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
