package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.mapper.AccountMapper;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.AccountTime;
import com.ufanet.meetingsbot.model.BotState;
import com.ufanet.meetingsbot.model.Settings;
import com.ufanet.meetingsbot.repository.AccountRepository;
import com.ufanet.meetingsbot.repository.AccountTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@EnableCaching
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"account", "group_members", "account_times"})
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountTimeRepository accountTimeRepository;

    @Cacheable(key = "#userId", value = "account", unless = "#result == null")
    public Optional<Account> getByUserId(long userId) {
        log.info("getting user {} from db", userId);
        return accountRepository.findById(userId);
    }

    public List<AccountTime> getAccountTimesByMeetingId(long meetingId) {
        log.info("getting account times with meeting {} from db", meetingId);
        return accountTimeRepository.findByMeetingId(meetingId);
    }

    @Transactional
    public void saveAccountTime(AccountTime accountTime) {
        log.info("saving account time {} into db", accountTime.getId());
        accountTimeRepository.save(accountTime);
    }

    @Transactional
    public void saveAccountTimes(List<AccountTime> accountTimes) {
        log.info("saving account times {} into db", accountTimes);
        accountTimeRepository.saveAll(accountTimes);
    }

    @Transactional
    @CacheEvict(key = "#account.id", value = "account")
    public void save(Account account) {
        log.info("saving user {} into db", account.getId());
        accountRepository.save(account);
    }

    @Cacheable(key = "#groupId", value = "group_members")
    public Set<Account> getAccountsByGroupsIdAndIdNot(long groupId, long userId) {
        log.info("getting members from group {} without member {}", groupId, userId);
        return accountRepository.findAccountByGroupsIdAndIdNot(groupId, userId);
    }

    public List<Account> getAccountsByMeetingId(long meetingId) {
        log.info("getting accounts from meeting {}", meetingId);
        return accountRepository.findAccountsByMeetingId(meetingId);
    }

    @Transactional
    public Account saveTgUser(User user) {
        log.info("saving telegram user {} into database", user.getId());
        Account account = Account.builder().id(user.getId())
                .firstname(user.getFirstName())
                .lastname(user.getLastName())
                .username(user.getUserName())
                .build();
        Settings settings = Settings.builder()
                .account(account).timeZone("UTC+03:00")
                .language("ru-RU").build();
        BotState botState = BotState.builder()
                .state(AccountState.PROFILE.name())
                .account(account)
                .build();

        account.setBotState(botState);
        account.setSettings(settings);
        save(account);
        return account;
    }

    @Transactional
    public void updateTgUser(Account account, User user) {
        log.info("updating account from telegram user {}", user.getId());
        account.setLastname(user.getLastName());
        account.setFirstname(user.getFirstName());
        account.setUsername(user.getUserName());
        save(account);
    }
}


