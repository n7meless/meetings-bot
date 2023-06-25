package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.EditState;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.repository.AccountTimeRepository;
import com.ufanet.meetingsbot.repository.MeetingTimeRepository;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.message.EditReplyMessageService;
import com.ufanet.meetingsbot.service.message.MeetingReplyMessageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.DateTimeException;
import java.time.LocalDateTime;

@Slf4j
@Component
@AllArgsConstructor
public class EditKeyboardHandler implements KeyboardHandler {
    private final MeetingService meetingService;
    private final MeetingReplyMessageService meetingReplyMessage;
    private final AccountService accountService;
    private final MeetingTimeRepository meetingTimeRepository;
    private final AccountTimeRepository accountTimeRepository;
    private final UpdateService updateService;
    private final BotService botService;
    private final EditReplyMessageService editReplyMessage;

    @Override
    public void handleUpdate(Update update) {
        UpdateDto updateDto = updateService.parseUpdate(update);
        long userId = updateDto.chatId();
        String content = updateDto.content();
        Meeting meeting = meetingService.getByOwnerIdAndStateNotReady(userId);

        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data.startsWith(AccountState.EDIT.name())) {
                EditState editState = EditState.valueOf(data);
                botService.setState(userId, editState);
            }
        }
        String botState = botService.getState(userId);
        EditState editState = EditState.valueOf(botState);

        handleToggleButton(userId, editState, meeting, content);

    }

    public void handleToggleButton(long userId, EditState state, Meeting meeting, String message) {
        ToggleButton toggleButton = ToggleButton.typeOf(message);
        switch (toggleButton) {
            case READY -> {
                botService.setState(userId, AccountState.CREATE);
            }
            case NEXT -> {
                handleMessage(userId, state.next(), meeting, message);
            }
            case CURRENT -> {
                if (!message.startsWith(AccountState.EDIT.name())) {
                    handleStep(userId, state, meeting, message);
                }
                handleMessage(userId, state, meeting, message);
            }
        }
    }

    void handleStep(long userId, EditState state, Meeting meeting, String callback) {
        try {
            switch (state) {
                case EDIT_GROUP -> meetingService.updateGroup(meeting, Long.parseLong(callback));
                case EDIT_PARTICIPANT -> meetingService.updateParticipants(meeting, Long.parseLong(callback));
                case EDIT_SUBJECT -> meetingService.updateSubject(meeting, callback);
                case EDIT_SUBJECT_DURATION -> meetingService.updateSubjectDuration(meeting, Integer.parseInt(callback));
                case EDIT_QUESTION -> meetingService.updateQuestion(meeting, callback);
                case EDIT_DATE -> meetingService.updateDate(meeting, callback);
                case EDIT_TIME -> meetingService.updateTime(meeting, callback);
                case EDIT_ADDRESS -> meetingService.updateAddress(meeting, callback);
            }
            meeting.setUpdatedDt(LocalDateTime.now());

        } catch (IllegalArgumentException | DateTimeException e) {
            log.debug("invalid value entered by user {}", userId);
        }
        botService.setState(userId, state);
        meetingService.saveOnCache(userId, meeting);
    }

    void handleMessage(long userId, EditState state, Meeting meeting, String callback) {
        switch (state) {
//            case EDIT_GROUP -> replyMessage.sendGroupMessage(userId, meeting);
            case EDIT_PARTICIPANT -> editReplyMessage.editParticipants(userId, meeting);
            case EDIT_SUBJECT -> editReplyMessage.editSubject(userId, meeting);
            case EDIT_SUBJECT_DURATION -> editReplyMessage.editSubjectDuration(userId, meeting);
            case EDIT_QUESTION -> editReplyMessage.editQuestion(userId, meeting);
            case EDIT_DATE -> editReplyMessage.editDate(userId, meeting, callback);
            case EDIT_TIME -> editReplyMessage.editTime(userId, meeting);
            case EDIT_ADDRESS -> editReplyMessage.editAddress(userId, meeting);
        }
    }


    @Override
    public AccountState getAccountStateHandler() {
        return AccountState.EDIT;
    }
}
