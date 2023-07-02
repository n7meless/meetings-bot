package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.AccountTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountTimeRepository extends JpaRepository<AccountTime, Long> {
    @Query(value = """
            select ut
            from user_times as ut join meeting_time mt on mt.id = ut.meetingTime.id
            join  meeting_date md on mt.meetingDate.id = md.id and md.meeting.id = ?1
            """)
    @EntityGraph(attributePaths = {"meetingTime", "meetingTime.meetingDate", "account"})
    List<AccountTime> findByMeetingId(Long meetingId);
//    join  meetings m on md.meeting.id = m.id and m.id=?1
}
