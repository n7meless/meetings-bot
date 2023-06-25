package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.Meeting;
import jakarta.persistence.QueryHint;
import org.hibernate.annotations.FetchProfile;
import org.hibernate.annotations.FetchProfiles;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

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

//        @Query(value = "SELECT md, mt, ut, m FROM meetings m " +
//            "LEFT join meeting_date md on m.id = md.meeting.id " +
//            "LEFT join meeting_time mt on md.id = mt.meetingDate.id " +
//            "LEFT join user_times ut on mt.id = ut.meetingTime.id "+
//            "WHERE m.owner.id =?1 and m.state NOT IN (?2)")
    @EntityGraph(value = "meeting-entity-graph", type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = "subject")
//    @QueryHints(value = {@QueryHint(name = org.hibernate.jpa.AvailableHints., value = "false" )})
//    @EntityGraph(attributePaths = {"subject","dates","dates.meetingTimes"})
    Optional<Meeting> findByOwnerIdAndStateIsNotIn(Long id, List<MeetingState> states);

    @EntityGraph(attributePaths = {"dates", "subject"})
    List<Meeting> findMeetingsByAccountMeetings_AccountIdOrOwnerIdEquals(Long participantId, Long ownerId);

    @EntityGraph(value = "meeting-entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    @Query(value = """
            FROM meetings m
                        join meeting_date md on m.id = md.meeting.id
                        join meeting_time mt on md.id = mt.meetingDate.id
                        join user_times ut on mt.id = ut.meetingTime.id
                        WHERE (ut.account.id=?1 or m.owner.id=?2) and m.state=?3
                        order by mt.time
            """)
    List<Meeting> findByAccountMeetingsIdOrOwnerIdAndStateEquals(Long participantId, Long ownerId, MeetingState state);

//    @EntityGraph(value = "meeting-entity-graph", type = EntityGraph.EntityGraphType.FETCH)
//    Optional<Meeting> findTopByIdAndParticipantsIdOrOwnerIdAndStateEquals(Long meetingId, Long participantId, Long ownerId, MeetingState state);

    void deleteByOwnerId(long ownerId);
}
