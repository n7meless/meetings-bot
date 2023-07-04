package com.ufanet.meetingsbot.scheduler;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.entity.Meeting;
import com.ufanet.meetingsbot.message.UpcomingReplyMessage;
import com.ufanet.meetingsbot.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingNotifierScheduler {
    private final UpcomingReplyMessage upcomingReplyMessage;
    private final MeetingService meetingService;


    @Scheduled(fixedRate = 60000)
    public void checkMeetings() {
        checkUpcomingMeetings();
        checkExpiredMeetings();
    }

    private void checkUpcomingMeetings() {
        ZonedDateTime now = ZonedDateTime.now();
        List<Meeting> meetings = meetingService.getConfirmedMeetingsWhereDatesBetween(now, 30);
        for (Meeting meeting : meetings) {

            LocalDateTime meetingLastUpdateTime = meeting.getUpdatedDt();

            long betweenNowAndMeetingUpdate = ChronoUnit.MINUTES.between(meetingLastUpdateTime, now);
            long betweenNowAndMeetingDate = ChronoUnit.MINUTES.between(now, meeting.getDate());

            if (betweenNowAndMeetingUpdate >= 30 && betweenNowAndMeetingDate < 30 && betweenNowAndMeetingDate > 10) {

                log.info("meeting {} notification sent 30 min before the start", meeting.getId());
                upcomingReplyMessage.sendComingMeetingNotifyToParticipants(meeting,
                        "upcoming.meeting.confirmed.coming30");
                meeting.setUpdatedDt(LocalDateTime.now());

                meetingService.save(meeting);
            } else if (betweenNowAndMeetingUpdate >= 10 && betweenNowAndMeetingDate < 10) {

                log.info("meeting {} notification sent 10 min before the start", meeting.getId());
                upcomingReplyMessage.sendComingMeetingNotifyToParticipants(meeting,
                        "upcoming.meeting.confirmed.coming10");
                meeting.setUpdatedDt(LocalDateTime.now());

                meetingService.save(meeting);

            }
        }
    }

    private void checkExpiredMeetings() {
        ZonedDateTime now = ZonedDateTime.now();
//        List<Meeting> expiredMeetings = meetingRepository.findConfirmedMeetingsWhereDatesLaterThan(now, 90);
        List<Meeting> expiredMeetings = meetingService.getConfirmedMeetingsWhereDatesLaterThanSubjectDuration(now);
        for (Meeting meeting : expiredMeetings) {
            log.info("meeting {} was expired", meeting.getId());
            meeting.setState(MeetingState.PASSED);
        }
        if (!expiredMeetings.isEmpty()) {
            meetingService.saveAll(expiredMeetings);
        }
    }
}
