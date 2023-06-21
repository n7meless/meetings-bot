package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.message.MeetingMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class CreateKeyboardHandler implements KeyboardHandler {
    private final MeetingService meetingService;
    private final MeetingMessageService messageHandler;

    @Override
    public void handleUpdate(Update update) {
        UpdateDto updateDto = UpdateService.parseUpdate(update);
        long userId = updateDto.chatId();
        String callback = updateDto.content();

        Meeting meeting = meetingService.getByOwnerIdAndStateNotReady(userId);
        if (update.hasCallbackQuery()) {
            switch (callback) {
                case "SEND" -> {
                    meetingService.updateAccountTimes(meeting);
                    messageHandler.sendMeetingToParticipants(meeting);
                    messageHandler.sendSuccessMessageParticipants(userId);
                }
                case "NEXT" -> {
                    meetingService.setNextState(meeting);
                    messageHandler.sendMessage(userId, meeting, callback);
                }
                case "CANCEL" -> {
                    messageHandler.sendCanceledMessage(userId);
                    meetingService.removeByOwnerId(userId);
                }
                default -> {
                    handleMeetingCallback(userId, callback, meeting);
                    messageHandler.sendMessage(userId, meeting, callback);
                }
            }
        } else if (update.hasMessage()) {
            handleMessage(userId, callback, meeting);
            messageHandler.sendMessage(userId, meeting, callback);
        }
    }

    public void handleMeetingCallback(long userId, String callback, Meeting meeting) {
        MeetingState state = meeting.getState();
        if (state != null) {
            switch (state) {
                case GROUP_SELECT -> meetingService.updateGroup(meeting, Long.valueOf(callback));
                case PARTICIPANT_SELECT -> meetingService.updateParticipants(meeting, Long.valueOf(callback));
                case SUBJECT_DURATION_SELECT -> meetingService.updateSubjectDuration(meeting, callback);
                case DATE_SELECT -> meetingService.updateDate(meeting, callback);
                case TIME_SELECT -> meetingService.updateTime(meeting, callback);
                //TODO удалить все про встречу
                case CANCELED -> meetingService.removeByOwnerId(userId);
            }
        } else meeting.setState(MeetingState.GROUP_SELECT);
    }

    public void handleMessage(long userId, String callback, Meeting meeting) {
        MeetingState state = meeting.getState();
        if (state != null) {
            switch (state) {
                case SUBJECT_SELECT -> meetingService.updateSubject(meeting, callback);
                case QUESTION_SELECT -> meetingService.updateQuestion(meeting, callback);
                case ADDRESS_SELECT -> meetingService.updateAddress(meeting, callback);
            }
        } else meeting.setState(MeetingState.GROUP_SELECT);
    }

    @Override
    public AccountState getUserStateHandler() {
        return AccountState.CREATE;
    }
}
