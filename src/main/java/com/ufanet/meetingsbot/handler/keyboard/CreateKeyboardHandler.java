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

import java.util.List;

import static com.ufanet.meetingsbot.constants.state.MeetingState.*;

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
        Meeting meeting = meetingService.getByOwnerIdAndStateNotContaining(userId, List.of(AWAITING, CONFIRMED, CANCELED));
        switch (callback) {
            case "SEND" -> {
                messageHandler.sendMeetingToParticipants(meeting);
                meetingService.setNextState(meeting);
                meetingService.save(meeting);
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
                meetingService.update(userId, meeting, callback);
                messageHandler.sendMessage(userId, meeting, callback);
            }
        }
    }

    @Override
    public AccountState getUserStateHandler() {
        return AccountState.CREATE;
    }
}
