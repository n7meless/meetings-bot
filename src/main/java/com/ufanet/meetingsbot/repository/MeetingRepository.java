package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.Meeting;
import jakarta.persistence.UniqueConstraint;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    @Override
    @EntityGraph(value = "client_entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Meeting> findById(Long aLong);

    Optional<Meeting> findByOwnerId(Long id);

    //    @Query(value = "SELECT md, mt, ut, m FROM meetings m " +
//            "join meeting_date md on m.id = md.meeting_id " +
//            "join meeting_time mt on md.id = mt.date_id " +
//            "join user_times ut on mt.id = ut.meeting_time_id "+
//            "WHERE m.state NOT IN ('CONFIRMED', 'AWAITING','CANCELED')", nativeQuery = true)
    @EntityGraph(value = "client_entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Meeting> findByOwnerIdAndStateIsNotIn(Long id, List<MeetingState> states);

    @EntityGraph(attributePaths = {"dates", "subject"})
    List<Meeting> findMeetingsByParticipantsIdOrOwnerIdEquals(Long participantId, Long ownerId);

    @EntityGraph(value = "client_entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    List<Meeting> findByParticipantsIdOrOwnerIdAndStateEquals(Long participantId, Long ownerId, MeetingState state);

    @EntityGraph(value = "client_entity-graph", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Meeting> findTopByIdAndParticipantsIdOrOwnerIdAndStateEquals(Long meetingId, Long participantId, Long ownerId, MeetingState state);

    void deleteByOwnerId(long ownerId);
}
