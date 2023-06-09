package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository  extends JpaRepository<Meeting, Long> {
}
