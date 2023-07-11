package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.entity.BotState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BotRepository extends JpaRepository<BotState, Long> {
    Optional<BotState> findByAccountId(long userId);
}
