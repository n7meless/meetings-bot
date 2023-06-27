package com.ufanet.meetingsbot.handler.event;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.message.MeetingReplyMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CreateEventHandler implements EventHandler {
    private final MeetingService meetingService;
    private final MeetingReplyMessageService messageService;
    private final BotService botService;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callback = callbackQuery.getData();
            Long userId = callbackQuery.getMessage().getChatId();
            Meeting meeting = meetingService.getByOwnerIdAndStateNotIn(userId,
                    List.of(MeetingState.CONFIRMED, MeetingState.AWAITING, MeetingState.CANCELED));
            handleCallback(userId, meeting, callback);
        }
        else if (update.hasMessage()) {
            String message = update.getMessage().getText();
            Long userId = update.getMessage().getChatId();
            Meeting meeting = meetingService.getByOwnerIdAndStateNotIn(
                    userId, List.of(MeetingState.CONFIRMED, MeetingState.AWAITING, MeetingState.CANCELED));

            if (!message.equals(AccountState.CREATE.getButtonName())) {
                handleStep(userId, meeting, message);
            }
            sendMessage(userId, meeting, message);
        }
    }

    protected void handleCallback(long userId, Meeting meeting, String message) {
        ToggleButton toggleButton = ToggleButton.typeOf(message);
        switch (toggleButton) {
            case SEND -> {
                meetingService.saveByOwner(meeting);
                messageService.sendMeetingToParticipants(meeting);
                messageService.sendMessageSentSuccessfully(userId);
            }
            case NEXT -> {
                MeetingState currState = meeting.getState();
                MeetingState newState = MeetingState.setNextState(currState);
                meeting.setState(newState);
                sendMessage(userId, meeting, message);
            }
            case CANCEL -> {
                meetingService.delete(meeting);
                messageService.sendCanceledMessage(userId);
            }
            case CURRENT -> {
                handleStep(userId, meeting, message);
                sendMessage(userId, meeting, message);
            }
        }
    }


    protected void handleStep(long userId, Meeting meeting, String callback) {
        try {
            MeetingState meetingState = meeting.getState();
            switch (meetingState) {
                case GROUP_SELECT -> meetingService.updateGroup(meeting, Integer.parseInt(callback));
                case PARTICIPANT_SELECT -> meetingService.updateParticipants(meeting, Long.parseLong(callback));
                case SUBJECT_SELECT -> {
                    meetingService.updateSubject(meeting, callback);
                    meeting.setState(MeetingState.SUBJECT_DURATION_SELECT);
                }
                case SUBJECT_DURATION_SELECT -> {
                    meetingService.updateSubjectDuration(meeting, Integer.parseInt(callback));
                    meeting.setState(MeetingState.QUESTION_SELECT);
                }
                case QUESTION_SELECT -> meetingService.updateQuestion(meeting, callback);
                case DATE_SELECT -> meetingService.updateDate(meeting, callback);
                case TIME_SELECT -> meetingService.updateTime(userId, meeting, callback);
                case ADDRESS_SELECT -> {
                    meetingService.updateAddress(meeting, callback);
                    meeting.setState(MeetingState.EDIT);
                }
            }
            meeting.setUpdatedDt(LocalDateTime.now());
        } catch (NumberFormatException | DateTimeException ex) {
            log.debug("invalid value entered by user {}", userId);
        } finally {
            meetingService.saveOnCache(userId, meeting);
        }
    }


    protected void sendMessage(long userId, Meeting meeting, String callback) {
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
            case EDIT -> messageService.sendAwaitingMessage(userId, meeting);
            case CANCELED -> messageService.sendCanceledMessage(userId);
        }
    }

    @Override
    public AccountState getAccountStateHandler() {
        return AccountState.CREATE;
    }
}
