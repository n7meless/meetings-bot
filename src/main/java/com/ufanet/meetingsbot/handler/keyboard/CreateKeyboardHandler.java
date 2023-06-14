package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.message.MeetingReplyMessageService;
import com.ufanet.meetingsbot.state.AccountState;
import com.ufanet.meetingsbot.state.MeetingState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class CreateKeyboardHandler implements KeyboardHandler {
    private final MeetingService meetingService;
    private final MeetingReplyMessageService messageHandler;

    @Override
    public void handleUpdate(Update update) {
        UpdateDto updateDto = UpdateService.parseUpdate(update);
        long chatId = updateDto.chatId();
        String content = updateDto.content();
        if (content.isBlank()) return;
        handleCallback(chatId, content);
    }

    void handleCallback(long userId, String callback) {
        switch (callback) {
            case "NEXT" -> {
                meetingService.setNextState(userId);
                handle(userId, callback);
            }
            case "CANCEL" -> {
                meetingService.removeByOwnerId(userId);
                messageHandler.sendCanceledMessage(userId);
            }
            default -> {
                meetingService.update(userId, callback);
                handle(userId, callback);
            }
        }
    }
    public void handle(long userId, String callback) {
        Meeting meeting = meetingService.getByOwnerId(userId);
        MeetingState state = meeting.getState();
        switch (state) {
            case GROUP_SELECTION -> messageHandler.sendGroupMessage(userId, meeting);
            case PARTICIPANTS_SELECTION -> messageHandler.sendParticipantsMessage(userId, meeting);
            case SUBJECT_SELECTION -> messageHandler.sendSubjectMessage(userId);
            case SUBJECT_DURATION_SELECTION -> messageHandler.sendSubjectDurationMessage(userId);
            case QUESTION_SELECTION -> messageHandler.sendQuestionMessage(userId, meeting);
            case DATE_SELECTION -> messageHandler.sendDateMessage(userId, meeting, callback);
            case TIME_SELECTION -> messageHandler.sendTimeMessage(userId, meeting);
            case ADDRESS_SELECTION -> messageHandler.sendAddressMessage(userId, meeting);
            case AWAITING -> messageHandler.sendAwaitingMessage(userId, meeting);
        }
    }

    @Override
    public AccountState getUserStateHandler() {
        return AccountState.CREATE;
    }
}
