package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.constants.type.EventType;
import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.entity.AccountTime;
import com.ufanet.meetingsbot.entity.BotState;
import com.ufanet.meetingsbot.entity.Settings;
import com.ufanet.meetingsbot.mapper.AccountMapper;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@EnableCaching
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"account", "group_members"})
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountTimeRepository accountTimeRepository;

    @Cacheable(key = "#userId", value = "account", unless = "#result == null")
    public Optional<Account> getByUserId(long userId) {
        log.info("getting user {} from db", userId);
        return accountRepository.findById(userId);
    }

    @Transactional
    public Account createAccount(User user) {
        log.info("saving telegram user {} into database", user.getId());
        Account account = AccountMapper.MAPPER.mapToEntityFromTgUser(user);

        Settings settings = Settings.builder()
                .account(account).zoneId("UTC+03:00")
                .language("ru-RU").build();
        BotState botState = BotState.builder()
                .state(EventType.PROFILE.name())
                .updatedDt(LocalDateTime.now())
                .account(account)
                .build();

        account.setBotState(botState);
        account.setSettings(settings);
        return save(account);
    }

    @Transactional
    public Account updateFromTgUser(Account account, User user) {
        log.info("updating account {} from telegram user", user.getId());
        account.setLastname(user.getLastName());
        account.setFirstname(user.getFirstName());
        account.setUsername(user.getUserName());
        return save(account);
    }

    @Transactional
    @CacheEvict(key = "#account.id", value = "account")
    public Account save(Account account) {
        log.info("saving user {} into db", account.getId());
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#groupId", value = "group_members")
    public Set<Account> getAccountsByGroupsIdAndIdNot(long groupId, long userId) {
        log.info("getting members from group {} without member {}", groupId, userId);
        return accountRepository.findAccountByGroupsIdAndIdNot(groupId, userId);
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByMeetingId(long meetingId) {
        log.info("getting accounts from meeting {}", meetingId);
        return accountRepository.findAccountsByMeetingId(meetingId);
    }

    @Transactional
    public List<AccountTime> saveAccountTimes(List<AccountTime> accountTimes) {
        log.info("saving account times {} into db", accountTimes);
        return accountTimeRepository.saveAll(accountTimes);
    }

    public List<AccountTime> getAccountTimesByMeetingId(long meetingId) {
        log.info("getting account times with meeting {} from db", meetingId);
        return accountTimeRepository.findByMeetingId(meetingId);
    }

    @Transactional
    public AccountTime saveAccountTime(AccountTime accountTime) {
        log.info("saving account time {} into db", accountTime.getId());
        return accountTimeRepository.save(accountTime);
    }

    public List<AccountTime> getAccountTimesByAccountAndMeetingId(long userId, long meetingId) {
        log.info("getting account times with user {} and meeting {} from db", userId, meetingId);
        return accountTimeRepository.findByAccountAndMeetingId(userId, meetingId);
    }
}


