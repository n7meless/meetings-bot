package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.AccountTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountTimeRepository extends JpaRepository<AccountTime, Long> {
    @Query(value = """
            select ut
            from user_times as ut join meeting_time mt on mt.id = ut.meetingTime.id
            join  meeting_date md on mt.meetingDate.id = md.id
            join  meetings m on md.meeting.id = m.id and m.id=?1
            """)
    List<AccountTime> findByMeetingId(Long meetingId);

    @Query(value = """
            select ut.id, ut.meeting_time_id, ut.status, ut.user_id
            from user_times as ut
                     join meeting_time mt on mt.id = ut.meeting_time_id
                     join meeting_date md on md.id = mt.date_id
                     join meetings m on ut.user_id =?1 and m.id = ?2 and m.id = md.meeting_id
                                    """, nativeQuery = true)
    List<AccountTime> findByAccountIdAndMeetingId(Long userId, Long meetingId);
}
