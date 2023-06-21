package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.message.MeetingReplyMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class CreateKeyboardHandler implements KeyboardHandler {
    private final MeetingService meetingService;
    private final MeetingReplyMessageService messageHandler;
    private final UpdateService updateService;

    @Override
    public void handleUpdate(Update update) {
        UpdateDto updateDto = updateService.parseUpdate(update);
        long userId = updateDto.chatId();
        String message = updateDto.content();

        Meeting meeting = meetingService.getByOwnerIdAndStateNotReady(userId);

        if (update.hasCallbackQuery()) {
            handleToggleButton(userId, message, meeting);
        } else if (update.hasMessage()) {
            handleMessage(message, meeting);
            messageHandler.sendMessage(userId, meeting, message);
        }
    }

    public void handleToggleButton(long userId, String message, Meeting meeting) {
        ToggleButton toggleButton = ToggleButton.typeOf(message);
        switch (toggleButton) {
            case SEND -> {
                meetingService.updateAccountTimes(meeting);
                messageHandler.sendMeetingToParticipants(meeting);
                messageHandler.sendSuccessMessageParticipants(userId);
            }
            case NEXT -> {
                meetingService.setNextState(meeting);
                messageHandler.sendMessage(userId, meeting, message);
            }
            case CANCEL -> {
                messageHandler.sendCanceledMessage(userId);
                meetingService.removeByOwnerId(userId);
            }
            case CURRENT -> {
                handleMeetingCallback(userId, message, meeting);
                messageHandler.sendMessage(userId, meeting, message);
            }
        }
    }

    public void handleMeetingCallback(long userId, String callback, Meeting meeting) {
        MeetingState state = meeting.getState();
        switch (state) {
            case GROUP_SELECT -> meetingService.updateGroup(meeting, Long.valueOf(callback));
            case PARTICIPANT_SELECT -> meetingService.updateParticipants(meeting, Long.valueOf(callback));
            case SUBJECT_DURATION_SELECT -> meetingService.updateSubjectDuration(meeting, callback);
            case DATE_SELECT -> meetingService.updateDate(meeting, callback);
            case TIME_SELECT -> meetingService.updateTime(meeting, callback);
            case CANCELED -> meetingService.removeByOwnerId(userId);
        }
    }

    public void handleMessage(String message, Meeting meeting) {
        MeetingState state = meeting.getState();
        switch (state) {
            case SUBJECT_SELECT -> meetingService.updateSubject(meeting, message);
            case QUESTION_SELECT -> meetingService.updateQuestion(meeting, message);
            case ADDRESS_SELECT -> meetingService.updateAddress(meeting, message);
        }
    }

    @Override
    public AccountState getUserStateHandler() {
        return AccountState.CREATE;
    }
}
