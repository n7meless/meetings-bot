package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.repository.AccountTimeRepository;
import com.ufanet.meetingsbot.repository.MeetingTimeRepository;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.message.MeetingReplyMessageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@AllArgsConstructor
public class EditKeyboardHandler implements KeyboardHandler {
    private final MeetingService meetingService;
    private final MeetingReplyMessageService messageHandler;
    private final AccountService accountService;
    private final MeetingTimeRepository meetingTimeRepository;
    private final AccountTimeRepository accountTimeRepository;
    private final UpdateService updateService;
    @Override
    public void handleUpdate(Update update) {
        UpdateDto updateDto = updateService.parseUpdate(update);
        long userId = updateDto.chatId();
        String content = updateDto.content();
//        Meeting meeting = meetingService.getByMeetingId(userId).orElseThrow();
//        if (meeting == null){
//            messageHandler.sendEditErrorMessage(userId);
//        }
        MeetingState state = MeetingState.typeOf(content);
        if (update.hasCallbackQuery()) {
            if (content.startsWith("Изменить")) {
                Meeting meeting = meetingService.getByOwnerId(userId);
                meeting.setState(MeetingState.SUBJECT_SELECT);
                messageHandler.sendSubjectMessage(userId, meeting);
            }
             else {
//                Meeting meeting = meetingService.getByMeetingIdOrUserId(userId).orElseThrow();
//                switch (state) {
//                    case GROUP_SELECT -> {
//                        meetingService.updateGroup(meeting, Long.valueOf(content));
//                        messageHandler.sendParticipantsMessage(userId, meeting);
//                        meetingService.setNextState(meeting);
//                    }
//                    case PARTICIPANT_SELECT -> {
//                        meetingService.updateParticipants(meeting, Long.valueOf(content));
//                        messageHandler.sendParticipantsMessage(userId, meeting);
//                    }
//                }
            }
        }
    }

    public void handleCallback(CallbackQuery callbackQuery) {

    }


    @Override
    public AccountState getUserStateHandler() {
        return AccountState.EDIT;
    }
}
