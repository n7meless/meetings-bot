package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.model.*;
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
@CacheConfig(cacheNames = {"account", "group_members"})
@EnableCaching
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountTimeRepository accountTimeRepository;
    private final BotService botService;

    @Cacheable(key = "#userId", value = "account", unless = "#result == null")
    public Optional<Account> getByUserId(long userId) {
        log.info("retrieving user {} from database", userId);
        return accountRepository.findById(userId);
    }

    public List<AccountTime> getAccountTimesByMeetingId(long meetingId){
        return accountTimeRepository.findByMeetingId(meetingId);
    }

    //    @Cacheable(cacheNames = "account_times", key = "#userId",unless = "#result == null")
    public Optional<AccountTime> getAccountTimesByUserIdAndMeetingId(long userId, long meetingId) {
        return accountTimeRepository.findByAccountIdAndMeetingId(userId, meetingId);
    }

    @Transactional
    public void saveAccountTime(AccountTime accountTime) {
        accountTimeRepository.save(accountTime);
    }

    @Transactional
    public void saveAccountTimes(List<AccountTime> accountTimes) {
        accountTimeRepository.saveAll(accountTimes);
    }
    @Transactional
    @CacheEvict(key = "#account.id", value = "account")
    public void save(Account account) {
        log.info("saving user {} to database", account.getId());
        accountRepository.save(account);
    }

    @Cacheable(key = "#groupId", value = "group_members")
    public Set<Account> getAccountByGroupsIdAndIdNot(long groupId, long userId) {
        return accountRepository.findAccountByGroupsIdAndIdNot(groupId, userId);
    }
    public List<Account> getAccountByMeetingId(long meetingId){
        return accountRepository.findAccountsByMeetingId(meetingId);
    }
    @Transactional
    public Account saveTgUser(User user) {
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
        account.setLastname(user.getLastName());
        account.setFirstname(user.getFirstName());
        account.setUsername(user.getUserName());
        save(account);
    }

}
