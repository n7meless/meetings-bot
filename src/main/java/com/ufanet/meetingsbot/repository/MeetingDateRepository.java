package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.MeetingDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingDateRepository extends JpaRepository<MeetingDate, Long> {
    List<MeetingDate> findByMeetingId(Long meetingId);
}
