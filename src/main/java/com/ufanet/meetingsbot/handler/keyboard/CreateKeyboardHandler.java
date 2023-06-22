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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.DateTimeException;

import static com.ufanet.meetingsbot.constants.state.AccountState.CREATE_MEETING;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateKeyboardHandler implements KeyboardHandler {
    private final MeetingService meetingService;
    private final MeetingReplyMessageService messageService;
    private final UpdateService updateService;

    @Override
    public void handleUpdate(Update update) {
        UpdateDto updateDto = updateService.parseUpdate(update);
        long userId = updateDto.chatId();
        String message = updateDto.content();

        Meeting meeting = meetingService.getByOwnerIdAndStateNotReady(userId);
        if (message.equals(CREATE_MEETING.getButtonName())) {
            handleMessage(userId, meeting, message);
        } else if (update.hasCallbackQuery()) {
            handleToggleButton(userId, meeting, message);
        } else if (update.hasMessage()) {
            handleStep(userId, meeting, message);
            handleMessage(userId, meeting, message);
        }
    }

    public void handleToggleButton(long userId, Meeting meeting, String message) {
        ToggleButton toggleButton = ToggleButton.typeOf(message);
        switch (toggleButton) {
            case SEND -> {
                meetingService.saveByOwner(meeting);
                messageService.sendMeetingToParticipants(meeting);
                messageService.sendSuccessMessageParticipants(userId);
            }
            case NEXT -> {
                meetingService.setNextState(meeting);
                handleMessage(userId, meeting, message);
            }
            case CANCEL -> {
                messageService.sendCanceledMessage(userId);
                meetingService.removeByOwnerId(userId);
            }
            case CURRENT -> {
                handleStep(userId, meeting, message);
                handleMessage(userId, meeting, message);
            }
        }
    }

    public void handleStep(long userId, Meeting meeting, String callback) {
        MeetingState state = meeting.getState();
        try {
            switch (state) {
                case GROUP_SELECT -> meetingService.updateGroup(meeting, Long.parseLong(callback));
                case PARTICIPANT_SELECT -> meetingService.updateParticipants(meeting, Long.parseLong(callback));
                case SUBJECT_SELECT -> meetingService.updateSubject(meeting, callback);
                case SUBJECT_DURATION_SELECT ->
                        meetingService.updateSubjectDuration(meeting, Integer.parseInt(callback));
                case QUESTION_SELECT -> meetingService.updateQuestion(meeting, callback);
                case DATE_SELECT -> meetingService.updateDate(meeting, callback);
                case TIME_SELECT -> meetingService.updateTime(meeting, callback);
                case ADDRESS_SELECT -> meetingService.updateAddress(meeting, callback);
                case CANCELED -> meetingService.removeByOwnerId(userId);
            }
        } catch (IllegalArgumentException | DateTimeException e) {
            log.debug("invalid value entered by user {}", userId);
        }
        meetingService.saveOnCache(userId, meeting);
    }

    public void handleMessage(long userId, Meeting meeting, String callback) {
        MeetingState state = meeting.getState();
        switch (state) {
            case GROUP_SELECT -> messageService.sendGroupMessage(userId, meeting);
            case PARTICIPANT_SELECT -> messageService.sendParticipantsMessage(userId, meeting);
            case SUBJECT_SELECT -> messageService.sendSubjectMessage(userId, meeting);
            case SUBJECT_DURATION_SELECT -> messageService.sendSubjectDurationMessage(userId, meeting);
            case QUESTION_SELECT -> messageService.sendQuestionMessage(userId, meeting);
            case DATE_SELECT -> messageService.sendDateMessage(userId, meeting, callback);
            case TIME_SELECT -> messageService.sendTimeMessage(userId, meeting);
            case ADDRESS_SELECT -> messageService.sendAddressMessage(userId);
            case READY -> messageService.sendAwaitingMessage(userId, meeting);
            case CANCELED -> messageService.sendCanceledMessage(userId);
        }
    }

//    public void handleMessage(long userId, String message, Meeting meeting) {
//        MeetingState state = meeting.getState();
//        switch (state) {
//            case SUBJECT_SELECT -> meetingService.updateSubject(meeting, message);
//            case QUESTION_SELECT -> meetingService.updateQuestion(meeting, message);
//            case ADDRESS_SELECT -> meetingService.updateAddress(meeting, message);
//        }
//        meetingService.saveOnCache(userId, meeting);
//    }

    @Override
    public AccountState getAccountStateHandler() {
        return CREATE_MEETING;
    }
}
