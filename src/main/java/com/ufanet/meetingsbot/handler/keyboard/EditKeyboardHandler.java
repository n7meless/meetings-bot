package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.model.MeetingTime;
import com.ufanet.meetingsbot.repository.AccountTimeRepository;
import com.ufanet.meetingsbot.repository.MeetingTimeRepository;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.message.MeetingMessageService;
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
    private final MeetingMessageService messageHandler;
    private final MeetingTimeRepository meetingTimeRepository;
    @Override
    public void handleUpdate(Update update) {
        UpdateDto updateDto = UpdateService.parseUpdate(update);
        long userId = updateDto.chatId();
        String content = updateDto.content();
//        Meeting meeting = meetingService.getByMeetingId(userId).orElseThrow();
//        if (meeting == null){
//            messageHandler.sendEditErrorMessage(userId);
//        }
        MeetingState state = MeetingState.typeOf(content);
        if (update.hasCallbackQuery()){
            if (content.startsWith("Изменить")){
                Meeting meeting = meetingService.getByOwnerId(userId);
                meeting.setState(MeetingState.SUBJECT_SELECTION);
                messageHandler.sendSubjectMessage(userId, meeting);
            }
            else if (content.startsWith(AccountState.MEETING_CONFIRM.name())){
                String callback = content.substring(AccountState.MEETING_CONFIRM.name().length());
                String[] split = callback.split(" ");
                long meetingId = Long.parseLong(split[0]);
                long meetingTimeId = Long.parseLong(split[1]);
                MeetingTime meetingTime = meetingTimeRepository.findById(meetingTimeId).orElseThrow();
                Meeting meeting = meetingService.getByMeetingId(meetingId).orElseThrow();

                //TODO throw exception
                meetingService.updateMeetingAccountTime(userId, meeting);
                messageHandler.sendMeetingToParticipant(userId, meeting);
            }
            else {
                Meeting meeting = meetingService.getByMeetingId(userId).orElseThrow();
                switch (state){
                    case GROUP_SELECTION -> {
                        meetingService.updateGroup(meeting, Long.valueOf(content));
                        messageHandler.sendParticipantsMessage(userId, meeting);
                        meetingService.setNextState(meeting);
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
