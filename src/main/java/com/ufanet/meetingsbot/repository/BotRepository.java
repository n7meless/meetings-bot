package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.entity.BotState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotRepository extends JpaRepository<BotState, Long> {
    Optional<BotState> findByAccountId(long userId);
}
