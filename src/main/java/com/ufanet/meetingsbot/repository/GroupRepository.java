package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.entity.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query(value = "FROM chat ch JOIN ch.members mb WHERE mb.id = ?1")
    List<Group> findGroupsByMemberId(Long userId);

    @Override
    @EntityGraph(attributePaths = "members")
    Optional<Group> findById(Long groupId);
}
