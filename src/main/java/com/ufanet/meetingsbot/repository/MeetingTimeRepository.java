package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.entity.MeetingTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingTimeRepository extends JpaRepository<MeetingTime, Long> {

    @Query(value = """
            FROM meeting_time mt
            JOIN meeting_date md ON md.meeting.id = ?1 AND mt.meetingDate.id = md.id
            WHERE 'CONFIRMED' = ALL (SELECT CAST(uts.status AS string) FROM user_times uts 
            WHERE mt.id = uts.meetingTime.id)
            ORDER BY mt.dateTime
            LIMIT 1
            """)
    @EntityGraph(attributePaths = {"meetingDate"})
    Optional<MeetingTime> findByMeetingIdAndAllInStatus(Long meetingId, Status status);

    @Query(value = """
            FROM meeting_time mt JOIN meeting_date md ON mt.meetingDate.id = md.id
            JOIN meeting m ON m.id=md.meeting.id AND m.id=?1 AND m.state='PASSED' AND
            datediff(MINUTE,mt.dateTime, ?2) > 10
            """)
    List<MeetingTime> findPassedMeetingByMeetingId(Long meetingId, LocalDateTime dt1);

}
