package com.ufanet.meetingsbot.handler.event.impl;

import com.ufanet.meetingsbot.cache.impl.MeetingStateCache;
import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.GroupDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.SubjectDto;
import com.ufanet.meetingsbot.exceptions.UserNotFoundException;
import com.ufanet.meetingsbot.handler.event.EventHandler;
import com.ufanet.meetingsbot.mapper.GroupMapper;
import com.ufanet.meetingsbot.mapper.MeetingMapper;
import com.ufanet.meetingsbot.service.MeetingConstructor;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.GroupService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.message.MeetingReplyMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CreateEventHandler implements EventHandler {
    private final MeetingService meetingService;
    private final MeetingConstructor meetingConstructor;
    private final MeetingReplyMessageService messageService;
    private final MeetingStateCache meetingStateCache;
    private final AccountService accountService;
    private final GroupService groupService;
    private final GroupMapper groupMapper;
    private final MeetingMapper meetingMapper;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()) {
            long userId = getUserIdFromUpdate(update);

            MeetingDto meetingDto = meetingStateCache.get(userId);
            if (meetingDto == null) {
                Optional<Meeting> optionalMeeting = meetingService.getLastChangedMeetingByOwnerId(userId);
                Account account = accountService.getByUserId(userId).orElseThrow(UserNotFoundException::new);
                meetingDto = meetingMapper.mapIfPresentOrElseGet(optionalMeeting,
                        () -> meetingConstructor.create(account));
                meetingStateCache.save(userId, meetingDto);
            }

            if (update.hasMessage()) {
                String message = update.getMessage().getText();

                if (message.equals(AccountState.CREATE.getButtonName())) {
                    meetingStateCache.evict(userId);
                } else handleStep(userId, meetingDto, message);

                sendMessage(userId, meetingDto, message);
            } else {
                String callback = update.getCallbackQuery().getData();
                handleCallback(userId, meetingDto, callback);
            }
        }
    }

    private long getUserIdFromUpdate(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else return update.getCallbackQuery().getFrom().getId();
    }

    protected void handleCallback(long userId, MeetingDto meetingDto, String message) {
        ToggleButton toggleButton = ToggleButton.typeOf(message);
        switch (toggleButton) {
            case SEND -> {
                Meeting meeting = meetingMapper.map(meetingDto);
                meetingService.createMeeting(meeting);
                meetingDto.setId(meeting.getId());
                messageService.sendMeetingToParticipants(meetingDto);
                messageService.sendMessageSentSuccessfully(userId);
                meetingStateCache.evict(userId);
            }
            case NEXT -> {
                MeetingState currState = meetingDto.getState();
                MeetingState newState = MeetingState.setNextState(currState);
                meetingDto.setState(newState);
                sendMessage(userId, meetingDto, message);
            }
            case CANCEL -> {
                meetingStateCache.evict(userId);
                Meeting meeting = meetingMapper.map(meetingDto);
                meetingService.deleteById(meeting.getId());
                messageService.sendCanceledMessage(userId);
            }
            case CURRENT -> {
                handleStep(userId, meetingDto, message);
                sendMessage(userId, meetingDto, message);
            }
        }
    }


    protected void handleStep(long userId, MeetingDto meetingDto, String callback) {
        try {
            MeetingState meetingState = meetingDto.getState();
            switch (meetingState) {
                case GROUP_SELECT -> {
                    int groupId = Integer.parseInt(callback);
                    Group group = groupService.getByGroupId(groupId).orElseThrow();
                    GroupDto groupDto = groupMapper.map(group);
                    meetingDto.setGroupDto(groupDto);
                    meetingDto.setState(MeetingState.PARTICIPANT_SELECT);
                }
                case PARTICIPANT_SELECT -> {
                    long participantId = Long.parseLong(callback);
                    Set<Account> accounts =
                            accountService.getAccountsByGroupsIdAndIdNot(meetingDto.getGroupDto().getId(),
                                    meetingDto.getOwner().getId());
                    meetingConstructor.updateParticipants(meetingDto, participantId, accounts);
                }
                case SUBJECT_SELECT -> {
                    SubjectDto subjectDto = new SubjectDto();
                    subjectDto.setTitle(callback);
                    meetingDto.setSubjectDto(subjectDto);
                    meetingDto.setState(MeetingState.SUBJECT_DURATION_SELECT);
                }
                case SUBJECT_DURATION_SELECT -> {
                    SubjectDto subjectDto = meetingDto.getSubjectDto();
                    subjectDto.setDuration(Integer.parseInt(callback));
                    meetingDto.setSubjectDto(subjectDto);
                    meetingDto.setState(MeetingState.QUESTION_SELECT);
                }
                case QUESTION_SELECT -> meetingConstructor.updateQuestion(meetingDto, callback);
                case DATE_SELECT -> meetingConstructor.updateDate(meetingDto, callback);
                case TIME_SELECT -> meetingConstructor.updateTime(meetingDto, callback);
                case ADDRESS_SELECT -> {
                    meetingDto.setAddress(callback);
                    meetingDto.setState(MeetingState.EDIT);
                }
            }
            meetingDto.setUpdatedDt(LocalDateTime.now());
        } catch (NumberFormatException | DateTimeException ex) {
            log.debug("invalid value entered by user {}", userId);
        } finally {
            meetingStateCache.save(userId, meetingDto);
        }
    }


    protected void sendMessage(long userId, MeetingDto meetingDto, String callback) {
        MeetingState state = meetingDto.getState();
        switch (state) {
            case GROUP_SELECT -> {
                messageService.sendGroupMessage(userId);
            }
            case PARTICIPANT_SELECT -> messageService.sendParticipantsMessage(userId, meetingDto);
            case SUBJECT_SELECT -> messageService.sendSubjectMessage(userId, meetingDto);
            case SUBJECT_DURATION_SELECT -> messageService.sendSubjectDurationMessage(userId, meetingDto);
            case QUESTION_SELECT -> messageService.sendQuestionMessage(userId, meetingDto);
            case DATE_SELECT -> messageService.sendDateMessage(userId, meetingDto, callback);
            case TIME_SELECT -> messageService.sendTimeMessage(userId, meetingDto);
            case ADDRESS_SELECT -> messageService.sendAddressMessage(userId);
            case EDIT -> {
                messageService.sendAwaitingMessage(userId, meetingDto);
            }
            case CANCELED -> messageService.sendCanceledMessage(userId);
        }
    }

    @Override
    public AccountState getAccountStateHandler() {
        return AccountState.CREATE;
    }
}
