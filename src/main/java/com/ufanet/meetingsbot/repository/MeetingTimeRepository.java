package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.MeetingTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingTimeRepository extends JpaRepository<MeetingTime, Long> {
    @Query(value = """
            select mt.id, mt.date_time, mt.date_id
            from meeting_time as mt
                     join meeting_date md on md.meeting_id = ?1 and mt.date_id = md.id
            where 'CONFIRMED' = all (select ut.status from user_times ut where mt.id = ut.meeting_time_id) 
            limit 1
            """, nativeQuery = true)
    Optional<MeetingTime> findByMeetingIdAndConfirmed(Long meetingId);

    @Query(value = """
            from meeting_time mt join meeting_date md on mt.meetingDate.id = md.id
            join meetings m on m.id=md.meeting.id and m.id=?1 and m.state='PASSED' and
            datediff(minute,mt.dateTime, ?2) > 10
            """)
    List<MeetingTime> findPassedMeetingByMeetingId(Long meetingId, LocalDateTime dt1);

}
