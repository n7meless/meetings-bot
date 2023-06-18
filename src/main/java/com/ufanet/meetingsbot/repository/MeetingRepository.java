package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Optional<Meeting> findByOwnerId(Long id);

    @Query(value = "SELECT m.id, owner_id, address, group_id, created_dt, state FROM meetings m " +
            "WHERE m.state NOT IN ('CONFIRMED', 'AWAITING','CANCELED')", nativeQuery = true)
    Optional<Meeting> findByOwnerIdAndStateNotReady(Long id);
}
