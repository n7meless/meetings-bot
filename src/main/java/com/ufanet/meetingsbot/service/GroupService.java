package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.model.Settings;
import com.ufanet.meetingsbot.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;

@Service
@CacheConfig(cacheNames = "group")
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final AccountService accountService;

//    @Cacheable(key = "#chatId", value = "group")
    public Optional<Group> getByChatId(long chatId) {
        return groupRepository.findById(chatId);
    }

    public Group saveTgChat(Chat tgChat) {
        Group group = Group.builder().id(tgChat.getId())
                .description(tgChat.getDescription())
                .title(tgChat.getTitle())
                .build();

        return save(group);
    }

//    @CachePut(key = "#group.id", value = "group")
    public Group save(Group group) {
       return groupRepository.save(group);
    }
    @Cacheable(key = "#userId", value = "groups")
    public List<Group> getGroupsByMemberId(long userId){
        return groupRepository.findGroupsByMemberId(userId);
    }

    //TODO доделать добавление если пользователь есть
    public void saveMembers(Group group, List<User> tgUsers) {
        Set<Account> members = group.getMembers();
        for (User tgUser : tgUsers) {
            if (!tgUser.getIsBot()) {

                Long userId = tgUser.getId();
                Optional<Account> optionalAccount = accountService.getByUserId(userId);

                if (optionalAccount.isEmpty()) {
                    Account account = accountService.saveTgUser(tgUser);
                    members.add(account);
                } else {
                    members.add(optionalAccount.get());
                }
            }
        }
        group.setMembers(members);
        save(group);
    }

    public void removeMember(Group group, User member) {
        Set<Account> accounts = group.getMembers();
        Account leftMember = accounts.stream()
                .filter((account) -> Objects.equals(account.getId(), member.getId()))
                .findFirst().orElseThrow();
        accounts.remove(leftMember);
        group.setMembers(accounts);
        save(group);
    }
}
