package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAccountByGroupsId(Long chatId);
}
