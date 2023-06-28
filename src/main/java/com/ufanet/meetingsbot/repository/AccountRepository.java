package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    @EntityGraph(attributePaths = {"settings"})
    Set<Account> findAccountByGroupsIdAndIdNot(Long chatId, Long userId);

    @Override
    @EntityGraph(attributePaths = {"settings"})
    Optional<Account> findById(Long id);

    @EntityGraph(attributePaths = {"settings"})
    @Query("from users u join user_meetings um on u.id = um.account.id " +
            "join user_settings us on u.id = us.account.id and um.meeting.id=?1")
    List<Account> findAccountsByMeetingId(Long meetingId);

//    @Query(value = """
//            select users from users join user_chat uc on users.id = uc.user_id and
//            uc.chat_id=?1 where users.id in (?2)
//            """, nativeQuery = true)
//    List<Account> findByGroupIdAndAccountsIdsIn(Long groupId, List<Long> accountIds);

}
