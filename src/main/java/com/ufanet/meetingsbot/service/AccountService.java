package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.AccountRepository;
import com.ufanet.meetingsbot.repository.AccountTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
//@CacheConfig(cacheNames = {"account", "account_times"})
@EnableCaching
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountTimeRepository accountTimeRepository;
    private final BotService botService;

    @Cacheable(key = "#userId", value = "account", unless = "#result == null")
    public Optional<Account> getByUserId(long userId) {
        return accountRepository.findById(userId);
    }

    @Transactional
    public List<Meeting> getMeetingsByUserId(long userId) {
        Account account = getByUserId(userId).orElseThrow();
        return account.getMeetings();
    }

    //    @Cacheable(cacheNames = "account_times", key = "#userId",unless = "#result == null")
    public List<AccountTime> getAccountTimesByUserIdAndMeetingId(long userId, long meetingId) {
        return accountTimeRepository.findByAccountIdAndMeetingId(userId, meetingId);
    }
    @Transactional
    public void saveAccountTime(AccountTime accountTime) {
        accountTimeRepository.save(accountTime);
    }

    public void saveAccountTimes(List<AccountTime> accountTimes) {
        accountTimeRepository.saveAll(accountTimes);
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

    @CacheEvict(key = "#account.id", value = "account")
    public void save(Account account) {
        accountRepository.save(account);
    }

    @Cacheable(key = "#groupId", value = "group_members")
    public Set<Account> getAccountByGroupsIdAndIdNot(long groupId, long userId) {
        return accountRepository.findAccountByGroupsIdAndIdNot(groupId, userId);
    }

    public Account saveTgUser(User user) {
        Account account = Account.builder().id(user.getId())
                .firstname(user.getFirstName())
                .lastname(user.getLastName())
                .username(user.getUserName())
                .build();
        Settings settings = Settings.builder().account(account)
                .language(user.getLanguageCode()).build();
        BotState botState = BotState.builder()
                .account(account)
                .build();

        account.setBotState(botState);
        account.setSettings(settings);
        save(account);
        return account;
    }

    @CachePut(key = "#account.id", value = "account")
    public void updateTgUser(Account account, User user) {
        account.setLastname(user.getLastName());
        account.setFirstname(user.getFirstName());
        account.setUsername(user.getUserName());
        Settings settings = account.getSettings();
        settings.setLanguage(user.getLanguageCode());
        account.setSettings(settings);
        save(account);
    }

    public void setState(long userId, AccountState accountState) {
        BotState botState = botService.getByUserId(userId);
        AccountState state = botState.getState();
        if (state == null || !state.equals(accountState)) {
            botState.setState(accountState);
            botService.save(botState);
        }
    }

    public AccountState getState(long userId) {
        BotState botState = botService.getByUserId(userId);
        return botState.getState();
    }

}
