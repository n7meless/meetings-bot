package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
//    @Query(value = """
//            select u.id, u.username, u.first_name, u.last_name, u.created_dt,
//            bs.id as bsid, bs.user_id, bs.msg_type, bs.msg_id, bs.state,
//            uc.user_id, uc.chat_id, us.id as usid, us.user_id, us.time_zone, us.language
//            from users u join user_chat uc on u.id = uc.user_id and uc.chat_id=?1 and user_id!=?2
//            join bot_state bs on u.id = bs.user_id
//            join user_settings us on bs.user_id = us.user_id
//            """, nativeQuery = true)
    @EntityGraph(value = "accounts_with_settings_and_botstate")
    Set<Account> findAccountByGroupsIdAndIdNot(Long chatId, Long userId);

    @Override
    @EntityGraph(value = "account_with_settings")
    Optional<Account> findById(Long aLong);
}
