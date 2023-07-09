package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.entity.Meeting;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Override
    @EntityGraph(value = "meeting-with-children")
    Optional<Meeting> findById(Long aLong);

    @EntityGraph(value = "meeting-with-children")
    Optional<Meeting> findByOwnerId(Long id);

    @EntityGraph(value = "meeting-with-children", attributePaths = {"owner.settings"})
    Optional<Meeting> findByOwnerIdAndStateIsNotIn(Long ownerId, List<MeetingState> states);

    @Query(value = """
            FROM meeting m
            JOIN m.participants mp ON mp.id = ?1
            WHERE m.state IN (?2)
            """)
    @EntityGraph(attributePaths = {"dates", "dates.meetingTimes"})
    List<Meeting> findMeetingsByUserIdAndStateIn(Long accountId, List<MeetingState> states);

    @Query(value = """
            FROM meeting m
            JOIN meeting_date md ON m.id = md.meeting.id
            JOIN meeting_time mt ON mt.meetingDate.id = md.id AND m.state = 'CONFIRMED'
            AND DATEDIFF(MINUTE, ?1, mt.dateTime)  BETWEEN 0 AND ?2           
                            """)
    @EntityGraph(attributePaths = {"dates", "dates.meetingTimes", "participants", "participants.settings"})
    List<Meeting> findConfirmedMeetingsWhereDateMinutesBetween(ZonedDateTime zonedDateTime, Integer endValue);

    @Query(value = """
            FROM meeting m
            JOIN meeting_date md ON m.id = md.meeting.id
            JOIN meeting_time mt ON mt.meetingDate.id = md.id AND m.state = 'CONFIRMED'
            AND DATEDIFF(MINUTE, mt.dateTime, ?1)  > ?2  
            """)
    List<Meeting> findConfirmedMeetingsWhereDatesLaterThan(ZonedDateTime zonedDateTime, Integer minutes);

    @Query(value = """
            FROM meeting m
            JOIN subject sb ON m.id = sb.id
            JOIN meeting_date md ON m.id = md.meeting.id
            JOIN meeting_time mt ON mt.meetingDate.id = md.id AND m.state = 'CONFIRMED'
            AND DATEDIFF(MINUTE, mt.dateTime, ?1)  > sb.duration 
            """)
    @EntityGraph(attributePaths = {"subject"})
    List<Meeting> findConfirmedMeetingsWhereDatesLaterThanSubjectDuration(ZonedDateTime now);
}
