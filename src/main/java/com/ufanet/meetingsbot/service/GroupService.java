package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@CacheConfig(cacheNames = "group")
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final AccountService accountService;

    @Cacheable(key = "#chatId", value = "group")
    public Optional<Group> getByChatId(long chatId) {
        return groupRepository.findById(chatId);
    }

    public Group saveTgChat(Chat tgChat) {
        Group group = Group.builder().id(tgChat.getId())
                .biography(tgChat.getBio())
                .description(tgChat.getDescription())
                .title(tgChat.getTitle())
                .firstName(tgChat.getFirstName())
                .lastName(tgChat.getLastName())
                .build();

        return save(group);
    }

    @CachePut(key = "#group.id", value = "group")
    public Group save(Group group) {
       return groupRepository.save(group);
    }


    public void saveMembers(Group group, List<User> tgUsers) {
        Set<Account> newMembers = new HashSet<>();
        for (User tgUser : tgUsers) {
            if (!tgUser.getIsBot()) {

                Long userId = tgUser.getId();
                Optional<Account> account = accountService.getByUserId(userId);

                if (account.isEmpty()) {
                    Account newAccount = Account.builder()
                            .id(tgUser.getId())
                            .lastname(tgUser.getLastName())
                            .firstname(tgUser.getFirstName())
                            .username(tgUser.getUserName()).build();
                    accountService.save(newAccount);
                    newMembers.add(newAccount);
                } else {
                    newMembers.add(account.get());
                }
            }
        }
        Set<Account> currentMembers = group.getMembers();
        currentMembers.addAll(newMembers);
        save(group);
    }

    public void removeMember(Group group, User member) {
        Set<Account> accounts = group.getMembers();
        Account leftMember = accounts.stream()
                .filter((account) -> account.getId() == member.getId())
                .findFirst().get();
        accounts.remove(leftMember);
        group.setMembers(accounts);
        save(group);
    }
}
