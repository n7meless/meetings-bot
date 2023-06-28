package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.Meeting;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    @Override
//    @EntityGraph(value = "meeting-entity-graph", type = EntityGraph.EntityGraphType.FETCH,
//            attributePaths = {"meetingtime-with-accounttimes"})
//    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH,
//            attributePaths = {"subject", "subject.questions", "dates",
//                    "dates.meetingTimes", "accountMeetings", "accountMeetings.account"})
    @EntityGraph(value = "meeting-entity-graph", type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = "subject")
    Optional<Meeting> findById(Long aLong);

    @EntityGraph(value = "meeting-entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Meeting> findByOwnerId(Long id);

    //            @Query(value = "SELECT md, mt, ut, m FROM meetings m " +
//            "LEFT join meeting_date md on m.id = md.meeting.id " +
//            "LEFT join meeting_time mt on md.id = mt.meetingDate.id " +
//            "LEFT join user_times ut on mt.id = ut.meetingTime.id "+
//            "WHERE m.owner.id =?1 and m.state NOT IN (?2)")
    @EntityGraph(value = "meeting-entity-graph", attributePaths = "subject")
    Optional<Meeting> findByOwnerIdAndStateIsNotIn(Long ownerId, List<MeetingState> states);

    @EntityGraph(value = "meeting-entity-graph")
    @Query(value = """
            FROM meetings m
                        JOIN user_meetings um ON m.id=um.meeting.id 
                        AND um.account.id = ?1 AND m.state IN (?2)
            """)
    List<Meeting> findByAccountMeetingsIdOrOwnerIdAndStateIn(Long accountId, List<MeetingState> states);

    @Query(value = """
            FROM meetings m
            JOIN meeting_date md ON m.id = md.meeting.id
            JOIN meeting_time mt ON mt.meetingDate.id = md.id AND m.state = 'CONFIRMED'
            AND DATEDIFF(MINUTE, ?1, mt.dateTime)  BETWEEN 0 AND ?2             
                            """)
    @EntityGraph(attributePaths = {"dates", "dates.meetingTimes", "accountMeetings", "accountMeetings.account"})
    List<Meeting> findConfirmedMeetingsWhereDatesBetween(ZonedDateTime zonedDateTime, Integer endValue);

    @Query(value = """
            FROM meetings m
            JOIN meeting_date md ON m.id = md.meeting.id
            JOIN meeting_time mt ON mt.meetingDate.id = md.id AND m.state = 'CONFIRMED'
            AND DATEDIFF(MINUTE, mt.dateTime, ?1)  > ?2  
            """)
    @EntityGraph(attributePaths = {"accountMeetings", "accountMeetings.account"})
    List<Meeting> findConfirmedMeetingsWhereDatesLaterThan(ZonedDateTime zonedDateTime, Integer minutes);
}
