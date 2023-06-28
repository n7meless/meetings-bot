package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.AccountMeeting;
import com.ufanet.meetingsbot.model.Subject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

public class MeetingDto {
    private Long id;
    private long ownerId;
    private Set<AccountMeeting> accountMeetings;
    private String address;
    private long groupId;
    private String subjectTitle;
    private int subjectDuration;
    private Set<String> questions;
    private LocalDateTime createdDt;
    private LocalDateTime updatedDt;
    private Subject subject;
    private MeetingState state;
    private Map<LocalDate, Set<ZonedDateTime>> datesMap;
}
