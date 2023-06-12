package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = "group")
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final AccountService accountService;

    @Cacheable(key = "#chatId", value = "group")
    public Optional<Group> getByChatId(long chatId) {
        return chatRepository.findById(chatId);
    }

    @CachePut(key = "#tgChat.id", value = "group")
    public void saveTgChat(Chat tgChat) {
        Group group = Group.builder().id(tgChat.getId())
                .biography(tgChat.getBio())
                .description(tgChat.getDescription())
                .title(tgChat.getTitle())
                .firstName(tgChat.getFirstName())
                .lastName(tgChat.getLastName())
                .build();

        save(group);
    }

    public void save(Group group) {
        chatRepository.save(group);
    }

    ;


    public void saveMembers(Group group, List<User> tgUsers) {
        List<Account> members = new ArrayList<>();
        for (User tgUser : tgUsers) {
            Long userId = tgUser.getId();
            Optional<Account> account = accountService.getById(userId);
            if (!tgUser.getIsBot() && account.isEmpty()) {
                Account newAccount = Account.builder()
                        .id(tgUser.getId())
                        .lastname(tgUser.getLastName())
                        .firstname(tgUser.getFirstName())
                        .username(tgUser.getUserName()).build();
                accountService.save(newAccount);
                members.add(newAccount);
            }
        }
        group.setMembers(members);
        save(group);
    }

    public void removeMember(Group group, User member) {
        List<Account> accounts = group.getMembers();
        Account leftMember = accounts.stream()
                .filter((account) -> account.getId() == member.getId())
                .findFirst().get();
        accounts.remove(leftMember);
        group.setMembers(accounts);
        save(group);
    }
}
