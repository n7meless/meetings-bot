package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.entity.Group;
import com.ufanet.meetingsbot.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"group"})
public class GroupService {
    private final GroupRepository groupRepository;
    private final AccountService accountService;

    public Optional<Group> getByGroupId(long groupId) {
        log.info("getting group {} from db", groupId);
        return groupRepository.findById(groupId);
    }

    @Transactional
    public void deleteById(long groupId) {
        log.info("deleting group {} from db", groupId);
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

    @Transactional
    public void saveMembers(Group group, List<User> tgUsers) {
        log.info("saving group {} members {} into db", group.getId(), tgUsers);
        for (User tgUser : tgUsers) {
            if (!tgUser.getIsBot()) {

                Long userId = tgUser.getId();
                Optional<Account> optionalAccount = accountService.getByUserId(userId);

                if (optionalAccount.isEmpty()) {
                    Account account = accountService.saveTgUser(tgUser);
                    group.addMember(account);
                } else {
                    group.addMember(optionalAccount.get());
                }
            }
        }
        groupRepository.save(group);
    }

    @Transactional
    public void removeMember(Group group, User member) {
        log.info("removing from db group {} member {}", group.getId(), member.getId());
        Set<Account> accounts = group.getMembers();
        Account leftMember = accounts.stream()
                .filter((account) -> Objects.equals(account.getId(), member.getId()))
                .findFirst().orElseThrow();
        accounts.remove(leftMember);

        if (accounts.isEmpty()) {
            deleteById(group.getId());
        } else {
            group.setMembers(accounts);
            groupRepository.save(group);
        }
    }
}
