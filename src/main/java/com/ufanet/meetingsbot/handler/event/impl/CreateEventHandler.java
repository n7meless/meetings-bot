package com.ufanet.meetingsbot.handler.event.impl;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.GroupDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.SubjectDto;
import com.ufanet.meetingsbot.exceptions.AccountNotFoundException;
import com.ufanet.meetingsbot.exceptions.GroupNotFoundException;
import com.ufanet.meetingsbot.handler.event.EventHandler;
import com.ufanet.meetingsbot.mapper.GroupMapper;
import com.ufanet.meetingsbot.mapper.MeetingMapper;
import com.ufanet.meetingsbot.message.MeetingReplyMessage;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.GroupService;
import com.ufanet.meetingsbot.service.MeetingConstructor;
import com.ufanet.meetingsbot.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CreateEventHandler implements EventHandler {
    private final MeetingService meetingService;
    private final MeetingConstructor meetingConstructor;
    private final MeetingReplyMessage replyMessage;
    private final AccountService accountService;
    private final GroupService groupService;
    private final GroupMapper groupMapper;
    private final MeetingMapper meetingMapper;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()) {
            long userId = getUserIdFromUpdate(update);

            Optional<Meeting> meeting = meetingService.getFromCache(userId);
            if (meeting.isEmpty()) {
                meeting = meetingService.getLastChangedMeetingByOwnerId(userId);
            }
            MeetingDto meetingDto = meetingMapper.mapIfPresentOrElseGet(meeting,
                    () -> {
                        Account owner = accountService.getByUserId(userId)
                                .orElseThrow(() -> new AccountNotFoundException(userId));
                        return meetingConstructor.create(owner);
                    });

            if (update.hasMessage()) {
                handleMessage(userId, update.getMessage(), meetingDto);
            } else {
                handleCallback(userId, update.getCallbackQuery(), meetingDto);
            }
        }
    }

    private void handleMessage(long userId, Message message, MeetingDto meetingDto) {
        String messageText = message.getText();

        if (messageText.equals(AccountState.CREATE.getButtonName())) {
            meetingService.clearCache(userId);
        } else handleStep(userId, meetingDto, messageText);

        sendMessage(userId, meetingDto, messageText);
    }

    private long getUserIdFromUpdate(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else return update.getCallbackQuery().getFrom().getId();
    }

    protected void handleCallback(long userId, CallbackQuery callbackQuery, MeetingDto meetingDto) {
        String data = callbackQuery.getData();
        ToggleButton toggleButton = ToggleButton.typeOf(data);
        switch (toggleButton) {
            case SEND -> {
                meetingService.clearCache(userId);
                Meeting meeting = meetingMapper.map(meetingDto);
                meetingService.createMeeting(meeting);
                meetingDto.setId(meeting.getId());
                replyMessage.sendMeetingToParticipants(meetingDto);
                replyMessage.sendMessageSentSuccessfully(userId);
            }
            case NEXT -> {
                MeetingState currState = meetingDto.getState();
                MeetingState newState = MeetingState.setNextState(currState);
                meetingDto.setState(newState);
                Meeting meeting = meetingMapper.map(meetingDto);
                meetingService.saveOnCache(userId, meeting);
                sendMessage(userId, meetingDto, data);
            }
            case CANCEL -> {
                meetingService.clearCache(userId);
                Meeting meeting = meetingMapper.map(meetingDto);
                meetingService.deleteById(meeting.getId());
                replyMessage.sendCanceledMessage(userId);
            }
            case CURRENT -> {
                handleStep(userId, meetingDto, data);
                sendMessage(userId, meetingDto, data);
            }
        }
    }


    protected void handleStep(long userId, MeetingDto meetingDto, String text) {
        try {
            MeetingState meetingState = meetingDto.getState();
            switch (meetingState) {
                case GROUP_SELECT -> {
                    int groupId = Integer.parseInt(text);
                    Group group = groupService.getByGroupId(groupId)
                            .orElseThrow(() -> new GroupNotFoundException(userId));
                    GroupDto groupDto = groupMapper.map(group);
                    meetingDto.setGroupDto(groupDto);
                    meetingDto.setState(MeetingState.PARTICIPANT_SELECT);
                }
                case PARTICIPANT_SELECT -> {
                    long participantId = Long.parseLong(text);
                    Set<Account> accounts =
                            accountService.getAccountsByGroupsIdAndIdNot(meetingDto.getGroupDto().getId(),
                                    meetingDto.getOwner().getId());
                    meetingConstructor.updateParticipants(meetingDto, participantId, accounts);
                }
                case SUBJECT_SELECT -> {
                    SubjectDto subjectDto = new SubjectDto();
                    subjectDto.setTitle(text);
                    meetingDto.setSubjectDto(subjectDto);
                    meetingDto.setState(MeetingState.SUBJECT_DURATION_SELECT);
                }
                case SUBJECT_DURATION_SELECT -> {
                    SubjectDto subjectDto = meetingDto.getSubjectDto();
                    subjectDto.setDuration(Integer.parseInt(text));
                    meetingDto.setSubjectDto(subjectDto);
                    meetingDto.setState(MeetingState.QUESTION_SELECT);
                }
                case QUESTION_SELECT -> meetingConstructor.updateQuestion(meetingDto, text);
                case DATE_SELECT -> meetingConstructor.updateDate(meetingDto, text);
                case TIME_SELECT -> meetingConstructor.updateTime(meetingDto, text);
                case ADDRESS_SELECT -> {
                    meetingDto.setAddress(text);
                    meetingDto.setState(MeetingState.EDIT);
                }
            }
            meetingDto.setUpdatedDt(LocalDateTime.now());
        } catch (NumberFormatException | DateTimeException ex) {
            log.debug("invalid value entered by user {}", userId);
        } finally {
            Meeting meeting = meetingMapper.map(meetingDto);
            meetingService.saveOnCache(userId, meeting);
        }
    }


    protected void sendMessage(long userId, MeetingDto meetingDto, String callback) {
        MeetingState state = meetingDto.getState();
        switch (state) {
            case GROUP_SELECT -> replyMessage.sendGroupMessage(userId);
            case PARTICIPANT_SELECT -> replyMessage.sendParticipantsMessage(userId, meetingDto);
            case SUBJECT_SELECT -> replyMessage.sendSubjectMessage(userId);
            case SUBJECT_DURATION_SELECT -> replyMessage.sendSubjectDurationMessage(userId, meetingDto);
            case QUESTION_SELECT -> replyMessage.sendQuestionMessage(userId, meetingDto);
            case DATE_SELECT -> replyMessage.sendDateMessage(userId, meetingDto, callback);
            case TIME_SELECT -> replyMessage.sendTimeMessage(userId, meetingDto);
            case ADDRESS_SELECT -> replyMessage.sendAddressMessage(userId);
            case EDIT -> replyMessage.sendAwaitingMeetingMessage(userId, meetingDto);
            case CANCELED -> replyMessage.sendCanceledMessage(userId);
        }
    }

    @Override
    public AccountState getAccountStateHandler() {
        return AccountState.CREATE;
    }
}
