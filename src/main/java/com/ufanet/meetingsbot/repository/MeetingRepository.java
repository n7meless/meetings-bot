package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
@Repository
public interface MeetingRepository  extends JpaRepository<Meeting, Long> {
    Optional<Meeting> findByOwnerId(Long id);
//    @Query("select meetings from meetings where meetings.state not in ('CONFIRMED', 'AWAITING', 'CANCELED')")
    Optional<Meeting> findByOwnerIdAndStateNotIn(Long id, List<MeetingState> states);
}
