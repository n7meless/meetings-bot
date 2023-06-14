package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Set<Account> findAccountByGroupsIdAndIdNot(Long chatId, Long userId);
}
