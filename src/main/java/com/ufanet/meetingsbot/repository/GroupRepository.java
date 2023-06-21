package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query(value = "from chats ch join ch.members mb where mb.id = ?1")
    List<Group> findGroupsByMemberId(Long userId);
}