package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.model.MeetingTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingTimeRepository extends JpaRepository<MeetingTime, Long> {
    //    @Query(value = """
//            select mt.id, mt.date_time, mt.date_id, md.id as mdid, md.date, md.meeting_id,
//            ut.id as utid, ut.user_id, ut.meeting_time_id, ut.status
//            from meeting_time mt
//                     join meeting_date md on md.meeting_id = ?1 and mt.date_id = md.id
//                     join user_times ut on mt.id = ut.meeting_time_id
//            where 'CONFIRMED' = all (select ut.status from user_times ut where mt.id = ut.meeting_time_id)
//            order by mt.date_time
//            limit 1
//            """, nativeQuery = true)
    //TODO fix query
    @Query(value = """
            from meeting_time mt
                     join meeting_date md on md.meeting.id = ?1 and mt.meetingDate.id = md.id
                     join user_times ut on mt.id = ut.meetingTime.id
            where ut.status = all (select ut.status from user_times ut where mt.id = ut.meetingTime.id) 
            and ut.status = ?2
            order by mt.dateTime
            limit 1
            """)
    @EntityGraph(attributePaths = {"accountTimes", "accountTimes.account", "accountTimes.account.settings"})
    Optional<MeetingTime> findByMeetingIdAndAllInStatus(Long meetingId, Status status);

    @Query(value = """
            from meeting_time mt join meeting_date md on mt.meetingDate.id = md.id
            join meeting m on m.id=md.meeting.id and m.id=?1 and m.state='PASSED' and
            datediff(minute,mt.dateTime, ?2) > 10
            """)
    List<MeetingTime> findPassedMeetingByMeetingId(Long meetingId, LocalDateTime dt1);

}
