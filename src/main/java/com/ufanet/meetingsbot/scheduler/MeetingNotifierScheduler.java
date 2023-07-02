package com.ufanet.meetingsbot.scheduler;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.message.UpcomingReplyMessage;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingNotifierScheduler {
    private final UpcomingReplyMessage upcomingReplyMessage;
    private final MeetingRepository meetingRepository;


//    @Scheduled(fixedRate = 60000)
//    public void checkMeetings() {
//        checkUpcomingMeetings();
//        checkExpiredMeetings();
//    }

    private void checkUpcomingMeetings() {
        ZonedDateTime now = ZonedDateTime.now();
        List<Meeting> meetings = meetingRepository.findConfirmedMeetingsWhereDatesBetween(now, 10);
        for (Meeting meeting : meetings) {
            LocalDateTime updatedDt = meeting.getUpdatedDt();
            long between = ChronoUnit.MINUTES.between(updatedDt, now);
            if (between > 10) {
                upcomingReplyMessage.sendConfirmedComingMeeting(meeting);
                meeting.setUpdatedDt(LocalDateTime.now());
                meetingRepository.save(meeting);
            }
        }
    }

    private void checkExpiredMeetings() {
        ZonedDateTime now = ZonedDateTime.now();
//        List<Meeting> expiredMeetings = meetingRepository.findConfirmedMeetingsWhereDatesLaterThan(now, 90);
        List<Meeting> expiredMeetings = meetingRepository.findConfirmedMeetingsWhereDatesLaterThanSubjectDuration(now);
        for (Meeting meeting : expiredMeetings) {
            meeting.setState(MeetingState.PASSED);
            log.info("notification leave feedback about the meeting {}", meeting.getId());
            upcomingReplyMessage.sendCommentNotificationParticipants(meeting);
        }
        meetingRepository.saveAll(expiredMeetings);
    }
}
