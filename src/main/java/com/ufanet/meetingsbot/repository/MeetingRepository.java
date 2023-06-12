package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MeetingRepository  extends JpaRepository<Meeting, Long> {
    Optional<Meeting> findByOwnerId(Long id);
}
