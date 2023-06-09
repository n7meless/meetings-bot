package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;

    public void save(Meeting meeting) {
        meetingRepository.save(meeting);
    }

}
