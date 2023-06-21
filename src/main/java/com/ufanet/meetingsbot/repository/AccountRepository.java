package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Set<Account> findAccountByGroupsIdAndIdNot(Long chatId, Long userId);

    @Override
    @EntityGraph(value = "account_settings_botState")
    Optional<Account> findById(Long aLong);
}
