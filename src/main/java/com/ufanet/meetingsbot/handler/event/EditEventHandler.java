package com.ufanet.meetingsbot.handler.event;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.EditState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.repository.AccountTimeRepository;
import com.ufanet.meetingsbot.repository.MeetingTimeRepository;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.message.EditReplyMessageService;
import com.ufanet.meetingsbot.service.message.MeetingReplyMessageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class EditEventHandler implements EventHandler {
    private final MeetingService meetingService;
    private final MeetingReplyMessageService meetingReplyMessage;
    private final AccountService accountService;
    private final MeetingTimeRepository meetingTimeRepository;
    private final AccountTimeRepository accountTimeRepository;
    private final BotService botService;
    private final EditReplyMessageService editReplyMessage;

    @Override
    public void handleUpdate(Update update) {

        if (update.hasCallbackQuery()) {
            long userId = update.getCallbackQuery().getFrom().getId();
            String data = update.getCallbackQuery().getData();
            EditState editState;

            Meeting meeting = meetingService.getByOwnerIdAndStateNotIn(userId,
                    List.of(MeetingState.CONFIRMED, MeetingState.AWAITING, MeetingState.CANCELED));
            if (data.startsWith(AccountState.EDIT.name())) {
                editState = EditState.valueOf(data);
                botService.setState(userId, data);
            } else {
                String state = botService.getState(userId);
                editState = EditState.valueOf(state);
                handleStep(userId, editState, meeting, data);
            }
            handleToggleButton(userId, editState, meeting, data);

        } else if (update.hasMessage()) {
            long userId = update.getMessage().getChatId();
            String message = update.getMessage().getText();

            String botState = botService.getState(userId);
            EditState editState = EditState.valueOf(botState);
            Meeting meeting = meetingService.getByOwnerIdAndStateNotIn(userId,
                    List.of(MeetingState.CONFIRMED, MeetingState.AWAITING, MeetingState.CANCELED));

            handleStep(userId, editState, meeting, message);
            handleToggleButton(userId, editState, meeting, message);
        }
    }

    protected void handleToggleButton(long userId, EditState state, Meeting meeting, String message) {
        ToggleButton toggleButton = ToggleButton.typeOf(message);
        switch (toggleButton) {
            case READY -> {
                botService.setState(userId, AccountState.CREATE.name());
            }
            case NEXT -> {
                sendMessage(userId, state.next(), meeting);
            }
            case CURRENT -> {
                sendMessage(userId, state, meeting);
            }
        }
    }

    protected void handleStep(long userId, EditState state, Meeting meeting, String callback) {
        try {
            switch (state) {
                case EDIT_PARTICIPANT -> meetingService.updateParticipants(meeting, Long.parseLong(callback));
                case EDIT_SUBJECT -> meetingService.updateSubject(meeting, callback);
                case EDIT_SUBJECT_DURATION -> meetingService.updateSubjectDuration(meeting, Integer.parseInt(callback));
                case EDIT_QUESTION -> meetingService.updateQuestion(meeting, callback);
                case EDIT_DATE -> meetingService.updateDate(meeting, callback);
                case EDIT_TIME -> meetingService.updateTime(userId, meeting, callback);
                case EDIT_ADDRESS -> meetingService.updateAddress(meeting, callback);
            }
            meeting.setUpdatedDt(LocalDateTime.now());

        } catch (NumberFormatException | DateTimeException ex) {
            log.debug("invalid value entered by user {}", userId);
        } finally {
            meetingService.saveOnCache(userId, meeting);
        }
    }

    protected void sendMessage(long userId, EditState state, Meeting meeting) {
        switch (state) {
            case EDIT_PARTICIPANT -> editReplyMessage.editParticipants(userId, meeting);
            case EDIT_SUBJECT -> editReplyMessage.editSubject(userId, meeting);
            case EDIT_SUBJECT_DURATION -> editReplyMessage.editSubjectDuration(userId, meeting);
            case EDIT_QUESTION -> editReplyMessage.editQuestion(userId, meeting);
            case EDIT_TIME -> editReplyMessage.editTime(userId, meeting);
            case EDIT_ADDRESS -> editReplyMessage.editAddress(userId, meeting);
        }
    }

    @Override
    public AccountState getAccountStateHandler() {
        return AccountState.EDIT;
    }
}
