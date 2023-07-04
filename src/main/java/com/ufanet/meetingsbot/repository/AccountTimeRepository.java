package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.entity.AccountTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountTimeRepository extends JpaRepository<AccountTime, Long> {
    @Query(value = """
            SELECT ut
            FROM user_times AS ut JOIN meeting_time mt ON mt.id = ut.meetingTime.id AND ut.account.id = ?1
            JOIN  meeting_date md ON mt.meetingDate.id = md.id AND md.meeting.id = ?2
            """)
    @EntityGraph(attributePaths = {"meetingTime", "meetingTime.meetingDate", "account"})
    List<AccountTime> findByAccountAndMeetingId(Long userId, Long meetingId);

    @Query(value = """
            SELECT ut
            FROM user_times AS ut JOIN meeting_time mt ON mt.id = ut.meetingTime.id
            JOIN  meeting_date md ON mt.meetingDate.id = md.id AND md.meeting.id = ?1
            """)
    @EntityGraph(attributePaths = {"meetingTime", "meetingTime.meetingDate", "account"})
    List<AccountTime> findByMeetingId(Long meetingId);
}
