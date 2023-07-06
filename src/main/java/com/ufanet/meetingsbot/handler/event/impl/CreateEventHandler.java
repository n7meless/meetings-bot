package com.ufanet.meetingsbot.handler.event.impl;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.constants.type.EventType;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.GroupDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.SubjectDto;
import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.entity.Group;
import com.ufanet.meetingsbot.entity.Meeting;
import com.ufanet.meetingsbot.exceptions.AccountNotFoundException;
import com.ufanet.meetingsbot.exceptions.GroupNotFoundException;
import com.ufanet.meetingsbot.handler.event.EventHandler;
import com.ufanet.meetingsbot.mapper.AccountMapper;
import com.ufanet.meetingsbot.mapper.GroupMapper;
import com.ufanet.meetingsbot.mapper.MeetingMapper;
import com.ufanet.meetingsbot.message.MeetingReplyMessage;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.GroupService;
import com.ufanet.meetingsbot.service.MeetingConstructor;
import com.ufanet.meetingsbot.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateEventHandler implements EventHandler {
    private final MeetingService meetingService;
    private final MeetingConstructor meetingConstructor;
    private final MeetingReplyMessage replyMessage;
    private final AccountService accountService;
    private final GroupService groupService;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()) {
            long userId = getUserIdFromUpdate(update);

            Optional<Meeting> meeting = meetingService.getFromCache(userId);
            if (meeting.isEmpty()) {
                meeting = meetingService.getLastChangedMeetingByOwnerId(userId);
            }
            MeetingDto meetingDto = MeetingMapper.MAPPER.mapIfPresentOrElseGet(meeting,
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

        if (messageText.equals(EventType.CREATE.getButtonName())) {
            meetingService.clearCache(userId);
        } else
            handleStep(userId, meetingDto, messageText);

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
                log.info("sending created meeting to participants by user-owner {} ", userId);
                meetingService.clearCache(userId);
                Meeting meeting = MeetingMapper.MAPPER.mapToFullEntity(meetingDto);
                meetingService.createMeeting(meeting);
                meetingDto.setId(meeting.getId());
                replyMessage.sendMeetingToParticipants(meetingDto);
                replyMessage.sendMessageSentSuccessfully(userId);
            }
            case NEXT -> {
                log.info("meeting in state '{}' advances to the next state by user {}", meetingDto.getState(), userId);
                MeetingState currState = meetingDto.getState();
                MeetingState newState = MeetingState.setNextState(currState);
                meetingDto.setState(newState);
                Meeting meeting = MeetingMapper.MAPPER.mapToFullEntity(meetingDto);
                meetingService.saveOnCache(userId, meeting);
                sendMessage(userId, meetingDto, data);
            }
            case CANCEL -> {
                log.info("meeting in the state '{}' was canceled by user {}", meetingDto.getState(), userId);
                meetingService.clearCache(userId);
                Meeting meeting = MeetingMapper.MAPPER.mapToFullEntity(meetingDto);
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
            log.info("updating meeting by user {} on meeting state '{}'", userId, meetingState);
            switch (meetingState) {
                case GROUP_SELECT -> {
                    int groupId = Integer.parseInt(text);
                    Group group = groupService.getByGroupId(groupId)
                            .orElseThrow(() -> new GroupNotFoundException(userId));
                    GroupDto groupDto = GroupMapper.MAPPER.map(group);
                    meetingDto.setGroupDto(groupDto);
                    meetingDto.setState(MeetingState.PARTICIPANT_SELECT);
                }
                case PARTICIPANT_SELECT -> {
                    long participantId = Long.parseLong(text);
                    Set<AccountDto> groupMembers =
                            accountService.getAccountsByGroupsIdAndIdNot(meetingDto.getGroupDto().getId(),
                                            meetingDto.getOwner().getId()).stream().map(AccountMapper.MAPPER::mapWithSettings)
                                    .collect(Collectors.toSet());

                    meetingConstructor.updateParticipants(meetingDto, participantId, groupMembers);
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
            log.debug("invalid value '{}' entered by user {}", text, userId);
        } finally {
            Meeting meeting = MeetingMapper.MAPPER.mapToFullEntity(meetingDto);
            meetingService.saveOnCache(userId, meeting);
        }
    }


    protected void sendMessage(long userId, MeetingDto meetingDto, String callback) {
        MeetingState state = meetingDto.getState();
        log.info("sending message to user {} when meeting state is '{}'", userId, state);
        switch (state) {
            case GROUP_SELECT -> {
                List<Group> groups = groupService.getGroupsByMemberId(userId);
                replyMessage.sendGroupMessage(userId, groups);
            }
            case PARTICIPANT_SELECT -> {
                Set<AccountDto> members =
                        accountService.getAccountsByGroupsIdAndIdNot(meetingDto.getGroupDto().getId(), userId)
                                .stream().map(AccountMapper.MAPPER::mapWithSettings).collect(Collectors.toSet());
                replyMessage.sendParticipantsMessage(userId, meetingDto, members);
            }
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
    public EventType getEventType() {
        return EventType.CREATE;
    }
}
