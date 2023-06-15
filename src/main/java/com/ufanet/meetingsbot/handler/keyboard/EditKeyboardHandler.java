package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.message.MeetingReplyMessageService;
import com.ufanet.meetingsbot.constants.state.AccountState;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@AllArgsConstructor
public class EditKeyboardHandler implements KeyboardHandler {
    private final MeetingService meetingService;
    private final AccountService accountService;
    private final MeetingReplyMessageService messageHandler;
    @Override
    public void handleUpdate(Update update) {
        UpdateDto updateDto = UpdateService.parseUpdate(update);
        long userId = updateDto.chatId();
        String content = updateDto.content();
        Meeting meeting = meetingService.getByOwnerId(userId);
        if (meeting == null){
            messageHandler.sendEditErrorMessage(userId);
        }
        MeetingState state = MeetingState.typeOf(content);
        if (update.hasCallbackQuery()){
            if (content.startsWith("Изменить")){
                meeting.setState(MeetingState.SUBJECT_SELECTION);
                messageHandler.sendSubjectMessage(userId, meeting);
            }
            else {
                switch (state){
                    case GROUP_SELECTION -> {
                        meetingService.updateGroup(meeting, Long.valueOf(content));
                        messageHandler.sendParticipantsMessage(userId, meeting);
                        meetingService.setNextState(userId);
                    }
                    case PARTICIPANTS_SELECTION -> {
                        meetingService.updateParticipants(meeting, Long.valueOf(content));
                        messageHandler.sendParticipantsMessage(userId, meeting);
                    }
                }
            }
        }
    }
    public void handleCallback(CallbackQuery callbackQuery){

    }


    @Override
    public AccountState getUserStateHandler() {
        return AccountState.EDIT;
    }
}
