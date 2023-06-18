package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.AccountTime;
import com.ufanet.meetingsbot.model.MeetingTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AccountTimeRepository extends JpaRepository<AccountTime, Long> {
    @Query(value = """
            select (mt.id, mt.date_id, mt.status, mt.time) from meeting_time as mt join meetings on meetings.id=:meetingId""",
            nativeQuery = true)
    List<MeetingTime> findByMeeting(Long meetingId);
}
