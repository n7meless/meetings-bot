package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.UpcomingState;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.model.AccountTime;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.model.MeetingTime;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.message.MeetingMessageService;
import com.ufanet.meetingsbot.service.message.UpcomingMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.ufanet.meetingsbot.constants.ReplyKeyboardButton.UPCOMING_MEETINGS;
import static com.ufanet.meetingsbot.constants.UpcomingState.typeOf;

@Component
@RequiredArgsConstructor
public class UpcomingKeyboardHandler implements KeyboardHandler {
    private final UpcomingMessageService upcomingMessageService;
    private final MeetingService meetingService;
    private final MeetingMessageService messageHandler;

    @Override
    public void handleUpdate(Update update) {
        UpdateDto updateDto = UpdateService.parseUpdate(update);
        String content = updateDto.content();
        long userId = updateDto.chatId();
        if (content.startsWith(UPCOMING_MEETINGS.getButtonName())){
            upcomingMessageService.sendUpcomingMeetings(userId);
        }
        if (content.startsWith("UPCOMING")) {
            String[] splitContent = content.split(" ");
            UpcomingState state = typeOf(splitContent[0]);
            if (state == null) return;
            long meetingId = Long.parseLong(splitContent[1]);
            Meeting meeting = meetingService.getByMeetingIdOrUserId(userId, meetingId);
            List<AccountTime> accountTimes = meeting.getAccountTimeByUserId(userId);


            switch (state) {
                case UPCOMING_MEETINGS -> {
                    upcomingMessageService.sendUpcomingMeetingsByMeetingId(userId, meetingId);
                }
                case UPCOMING_EDIT_MEETING_TIME -> {

                    if (splitContent.length > 2) {
                        long accountTime = Long.parseLong(splitContent[2]);
                        meetingService.updateMeetingAccountTime(userId, meeting, accountTime, accountTimes);
                    }
                    messageHandler.editMeetingToParticipant(userId, meeting, accountTimes);
                }
                case UPCOMING_CANCEL_MEETING_TIME -> {
                    meetingService.updateMeetingAccountTimes(userId, accountTimes, true);
                    messageHandler.sendCanceledAccountTimeMessage(meeting);
                }
                case UPCOMING_CONFIRM_MEETING_TIME -> {
                    meetingService.updateMeetingAccountTimes(userId, accountTimes, false);
                    List<MeetingTime> confirmed = meetingService.getByMeetingIdAndConfirmedState(meetingId);
                    if (!confirmed.isEmpty()) {
                        meetingService.processConfirmedMeeting(meeting, confirmed);
                        messageHandler.sendReadyMeeting(meeting);
                    }else {
                        messageHandler.sendSuccessMeetingConfirm(userId);
                    }
                }
            }
        }
    }

    @Override
    public AccountState getUserStateHandler() {
        return AccountState.UPCOMING;
    }
}
