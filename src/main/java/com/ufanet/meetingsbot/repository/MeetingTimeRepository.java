package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.MeetingTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingTimeRepository extends JpaRepository<MeetingTime, Long> {
    @Query(value = """
            select mt.id, mt.time, mt.date_id
            from meeting_time as mt
                     join meeting_date md on md.meeting_id = ?1 and mt.date_id = md.id
            where 'CONFIRMED' = all (select ut.status from user_times ut where mt.id = ut.meeting_time_id)
            order by mt.time limit 1
            """, nativeQuery = true)
    Optional<MeetingTime> findByMeetingIdAndConfirmed(Long meetingId);

}
