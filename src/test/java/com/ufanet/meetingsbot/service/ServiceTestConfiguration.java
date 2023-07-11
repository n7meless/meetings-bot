package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.cache.impl.BotStateCache;
import com.ufanet.meetingsbot.cache.impl.MeetingCache;
import com.ufanet.meetingsbot.repository.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ServiceTestConfiguration {

    @Bean
    public AccountService accountService(AccountRepository accountRepository,
                                         AccountTimeRepository accountTimeRepository) {
        return new AccountService(accountRepository, accountTimeRepository);
    }

    @Bean
    public GroupService groupService(GroupRepository groupRepository) {
        return new GroupService(groupRepository);
    }

    @Bean
    public MeetingService meetingService(MeetingRepository meetingRepository,
                                         MeetingTimeRepository meetingTimeRepository) {
        return new MeetingService(meetingRepository, meetingTimeRepository, new MeetingCache());
    }

    @Bean
    public BotService botService(BotRepository botRepository) {
        return new BotService(botRepository, new BotStateCache());
    }
}
