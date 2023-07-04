package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    @EntityGraph(attributePaths = {"settings"})
    Set<Account> findAccountByGroupsIdAndIdNot(Long chatId, Long userId);

    @Override
    @EntityGraph(attributePaths = {"settings"})
    Optional<Account> findById(Long id);

    @Query("""
            FROM users u JOIN u.meetings um ON um.id= ?1
            JOIN user_settings us ON u.id = us.account.id
            """)
    @EntityGraph(attributePaths = {"settings"})
    List<Account> findAccountsByMeetingId(Long meetingId);
}
