package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.model.AccountTime;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.model.MeetingDate;
import com.ufanet.meetingsbot.model.MeetingTime;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.message.UpcomingReplyMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.ufanet.meetingsbot.constants.state.AccountState.UPCOMING_MEETINGS;
import static com.ufanet.meetingsbot.constants.state.UpcomingState.typeOf;

@Component
@RequiredArgsConstructor
public class UpcomingKeyboardHandler implements KeyboardHandler {
    private final UpcomingReplyMessageService messageService;
    private final MeetingService meetingService;
    private final UpdateService updateService;

    @Override
    public void handleUpdate(Update update) {
        UpdateDto updateDto = updateService.parseUpdate(update);
        String content = updateDto.content();
        long userId = updateDto.chatId();

        if (content.startsWith(UPCOMING_MEETINGS.getButtonName())) {
            List<Meeting> meetings = meetingService.getMeetingsByUserIdAndState(userId, MeetingState.CONFIRMED);
            if (meetings.isEmpty()) {
                messageService.sendMeetingsNotExist(userId);
            } else {
                messageService.sendSelectedUpcomingMeeting(userId, meetings);
            }
        }
        if (update.hasCallbackQuery() && content.startsWith("UPCOMING")) {
            handleCallback(userId, content);
        }
    }

    void handleCallback(long userId, String callback) {
        String[] splitContent = callback.split(" ");
        UpcomingState state = typeOf(splitContent[0]);
        if (state == null) return;
        long meetingId = Long.parseLong(splitContent[1]);

        Meeting meeting = meetingService.getByMeetingId(userId, meetingId);
        List<AccountTime> accountTimes = meeting.getAccountTimesByUserId(userId);

        switch (state) {
            case UPCOMING_MEETINGS -> {
                messageService.sendSelectedUpcomingMeeting(userId, meetingId);
            }
            case UPCOMING_EDIT_MEETING_TIME -> {
                if (splitContent.length > 2) {
                    long accTimeId = Long.parseLong(splitContent[2]);
                    meetingService.updateMeetingAccountTime(meeting, accTimeId, accountTimes);
                    meetingService.saveOnCache(userId, meeting);
                }
                messageService.editMeetingToParticipant(userId, meeting, accountTimes);
            }
            case UPCOMING_CANCEL_MEETING_TIME -> {
                meetingService.cancelMeeting(meeting);
                meetingService.saveOnCache(userId, meeting);
                messageService.sendCanceledAccountTimeMessage(meeting);
            }
            case UPCOMING_CONFIRM_MEETING_TIME -> {
                meetingService.confirmMeetingAccountTimes(accountTimes);

                boolean allVoted = meeting.getDates().stream().map(MeetingDate::getMeetingTimes)
                        .flatMap(Collection::stream).map(MeetingTime::getAccountTimes)
                        .flatMap(Collection::stream)
                        .allMatch(at -> at.getStatus().equals(Status.CONFIRMED) ||
                                at.getStatus().equals(Status.CANCELED));

                Optional<MeetingTime> confirmed = meetingService.getByMeetingIdAndConfirmedState(meetingId);
                if (allVoted && confirmed.isPresent()) {
                    meetingService.processConfirmedMeeting(userId, meeting, confirmed);
                    meetingService.clearCache(userId);
                    messageService.sendReadyMeeting(meeting);
                } else if (allVoted) {
                    meetingService.cancelMeeting(meeting);
                    meetingService.clearCache(userId);
                    messageService.sendCanceledMeetingByMatching(meeting);
                } else {
                    messageService.sendSuccessMeetingConfirm(userId);
                }
            }
        }
    }

    @Override
    public AccountState getAccountStateHandler() {
        return UPCOMING_MEETINGS;
    }
}
