package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@CacheConfig(cacheNames = {"group"})
@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final AccountService accountService;

    public Optional<Group> getByGroupId(long groupId) {
        log.info("getting group {} from db", groupId);
        return groupRepository.findById(groupId);
    }

    @Transactional
    public Group saveTgChat(Chat tgChat) {
        Group group = Group.builder().id(tgChat.getId())
                .description(tgChat.getDescription())
                .title(tgChat.getTitle())
                .build();

        return save(group);
    }

    @Transactional
    public void deleteById(long groupId) {
        groupRepository.deleteById(groupId);
    }

    @Transactional
    public Group save(Group group) {
        log.info("saving group {} and title '{}' into db", group.getId(), group.getTitle());
        return groupRepository.save(group);
    }

    public List<Group> getGroupsByMemberId(long userId) {
        log.info("getting group by member {} from db", userId);
        return groupRepository.findGroupsByMemberId(userId);
    }

    //TODO доделать добавление если пользователь есть
    @Transactional
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
        groupRepository.save(group);
    }

    @Transactional
    public void removeMember(Group group, User member) {
        Set<Account> accounts = group.getMembers();
        Account leftMember = accounts.stream()
                .filter((account) -> Objects.equals(account.getId(), member.getId()))
                .findFirst().orElseThrow();
        accounts.remove(leftMember);

        if (accounts.isEmpty()) {
            deleteById(group.getId());
        } else {
            group.setMembers(accounts);
            save(group);
        }
    }
}
