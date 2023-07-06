package com.ufanet.meetingsbot.handler.event.impl;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.EditState;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.SubjectDto;
import com.ufanet.meetingsbot.entity.Meeting;
import com.ufanet.meetingsbot.exceptions.MeetingNotFoundException;
import com.ufanet.meetingsbot.handler.event.EventHandler;
import com.ufanet.meetingsbot.mapper.AccountMapper;
import com.ufanet.meetingsbot.mapper.MeetingMapper;
import com.ufanet.meetingsbot.message.EditReplyMessage;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.MeetingConstructor;
import com.ufanet.meetingsbot.service.MeetingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class EditEventHandler implements EventHandler {
    private final MeetingService meetingService;
    private final AccountService accountService;
    private final BotService botService;
    private final EditReplyMessage editReplyMessage;
    private final MeetingConstructor meetingConstructor;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()) {
            long userId = getUserIdFromUpdate(update);
            Optional<Meeting> meeting = meetingService.getFromCache(userId);

            if (meeting.isEmpty()) {
                meeting = meetingService.getLastChangedMeetingByOwnerId(userId);
            }
            MeetingDto meetingDto = MeetingMapper.MAPPER.mapIfPresentOrElseThrow(meeting,
                    () -> new MeetingNotFoundException(userId));

            if (update.hasMessage()) {
                handleMessage(userId, update.getMessage(), meetingDto);
            } else {
                handleCallback(userId, update.getCallbackQuery(), meetingDto);
            }
        }
    }

    private long getUserIdFromUpdate(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else return update.getCallbackQuery().getFrom().getId();
    }

    private void handleCallback(long userId, CallbackQuery query, MeetingDto meetingDto) {
        String data = query.getData();

        EditState editState;

        if (data.startsWith(AccountState.EDIT.name())) {
            editState = EditState.valueOf(data);
            botService.setState(userId, data);
        } else {
            String state = botService.getState(userId);
            editState = EditState.valueOf(state);
            handleStep(userId, editState, meetingDto, data);
        }
        handleToggleButton(userId, editState, meetingDto, data);
    }

    private void handleMessage(long userId, Message message, MeetingDto meetingDto) {
        String messageText = message.getText();

        String botState = botService.getState(userId);
        EditState editState = EditState.valueOf(botState);

        handleStep(userId, editState, meetingDto, messageText);
        handleToggleButton(userId, editState, meetingDto, messageText);
    }

    protected void handleToggleButton(long userId, EditState state, MeetingDto meetingDto, String message) {
        ToggleButton toggleButton = ToggleButton.typeOf(message);
        switch (toggleButton) {
            case READY -> botService.setState(userId, AccountState.CREATE.name());
            case NEXT -> sendMessage(userId, state.next(), meetingDto);
            case CURRENT -> sendMessage(userId, state, meetingDto);
        }
    }

    protected void handleStep(long userId, EditState state, MeetingDto meetingDto, String text) {
        try {
            log.info("editing meeting by user {} on edit state '{}'", userId, state);
            switch (state) {
                case EDIT_PARTICIPANT -> {
                    long participantId = Long.parseLong(text);
                    Set<AccountDto> groupMembers =
                            accountService.getAccountsByGroupsIdAndIdNot(meetingDto.getGroupDto().getId(),
                                            meetingDto.getOwner().getId()).stream().map(AccountMapper.MAPPER::mapWithSettings)
                                    .collect(Collectors.toSet());

                    meetingConstructor.updateParticipants(meetingDto, participantId, groupMembers);
                }
                case EDIT_SUBJECT -> {
                    SubjectDto subjectDto = meetingDto.getSubjectDto();
                    subjectDto.setTitle(text);
                    meetingDto.setSubjectDto(subjectDto);
                }
                case EDIT_SUBJECT_DURATION -> {
                    SubjectDto subjectDto = meetingDto.getSubjectDto();
                    subjectDto.setDuration(Integer.parseInt(text));
                    meetingDto.setSubjectDto(subjectDto);
                }
                case EDIT_QUESTION -> meetingConstructor.updateQuestion(meetingDto, text);
                case EDIT_DATE -> meetingConstructor.updateDate(meetingDto, text);
                case EDIT_TIME -> meetingConstructor.updateTime(meetingDto, text);
                case EDIT_ADDRESS -> meetingDto.setAddress(text);
            }
            meetingDto.setUpdatedDt(LocalDateTime.now());
        } catch (NumberFormatException | DateTimeException ex) {
            log.debug("invalid value '{}' entered by user {}", text, userId);
        } finally {
            Meeting meeting = MeetingMapper.MAPPER.mapToFullEntity(meetingDto);
            meetingService.saveOnCache(userId, meeting);
        }
    }

    protected void sendMessage(long userId, EditState state, MeetingDto meetingDto) {
        log.info("sending message to user {} when edit state is '{}'", userId, state);
        switch (state) {
            case EDIT_PARTICIPANT -> {
                Set<AccountDto> members = accountService.getAccountsByGroupsIdAndIdNot(meetingDto.getGroupDto().getId(), userId)
                        .stream().map(AccountMapper.MAPPER::mapWithSettings).collect(Collectors.toSet());
                editReplyMessage.sendEditParticipants(userId, meetingDto, members);
            }
            case EDIT_SUBJECT -> editReplyMessage.semdEditSubject(userId, meetingDto);
            case EDIT_SUBJECT_DURATION -> editReplyMessage.sendEditSubjectDuration(userId, meetingDto);
            case EDIT_QUESTION -> editReplyMessage.sendEditQuestion(userId, meetingDto);
            case EDIT_TIME -> editReplyMessage.sendEditTime(userId, meetingDto);
            case EDIT_ADDRESS -> editReplyMessage.sendEditAddress(userId, meetingDto);
        }
    }

    @Override
    public AccountState getAccountStateHandler() {
        return AccountState.EDIT;
    }
}
