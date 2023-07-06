package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.entity.Group;
import com.ufanet.meetingsbot.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(key = "#groupId", value = "group")
    public Optional<Group> getByGroupId(long groupId) {
        log.info("getting group {} from db", groupId);
        return groupRepository.findById(groupId);
    }

    @Transactional
    @CacheEvict(key = "#groupId", value = "group")
    public void deleteById(long groupId) {
        log.info("deleting group {} from db", groupId);
        groupRepository.deleteById(groupId);
    }

    @Transactional
    @CacheEvict(key = "#group.id", value = "group")
    public Group save(Group group) {
        log.info("saving group {} and title '{}' into db", group.getId(), group.getTitle());
        return groupRepository.save(group);
    }

    public List<Group> getGroupsByMemberId(long userId) {
        log.info("getting group by member {} from db", userId);
        return groupRepository.findGroupsByMemberId(userId);
    }

    @Transactional
    public void removeMember(Group group, User member) {
        log.info("removing from db group {} member {}", group.getId(), member.getId());
        Set<Account> accounts = group.getMembers();
        accounts.removeIf((account) -> Objects.equals(account.getId(), member.getId()));

        if (accounts.isEmpty()) {
            deleteById(group.getId());
        } else {
            group.setMembers(accounts);
            save(group);
        }
    }
}
