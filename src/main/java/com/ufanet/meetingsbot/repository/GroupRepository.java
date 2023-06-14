package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
