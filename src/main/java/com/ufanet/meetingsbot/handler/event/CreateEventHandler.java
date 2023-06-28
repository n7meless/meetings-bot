package com.ufanet.meetingsbot.handler.event;

import com.ufanet.meetingsbot.cache.impl.MeetingStateCache;
import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.mapper.MeetingConstructor;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.message.MeetingReplyMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CreateEventHandler implements EventHandler {
    private final MeetingService meetingService;
    private final MeetingConstructor meetingConstructor;
    private final MeetingReplyMessageService messageService;
    private final MeetingStateCache meetingStateCache;
    private final BotService botService;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            String callback = update.getCallbackQuery().getData();
            Long userId = update.getCallbackQuery().getMessage().getChatId();
            MeetingDto meetingDto = meetingStateCache.get(userId);

            if (meetingDto == null) {
                Optional<Meeting> optionalMeeting = meetingService.getLastChangedMeetingByOwnerId(userId);
                meetingDto = meetingConstructor.mapIfPresentOrElseGet(optionalMeeting, () -> new MeetingDto(userId));
            }

            handleCallback(userId, meetingDto, callback);
        } else if (update.hasMessage()) {
            String message = update.getMessage().getText();
            Long userId = update.getMessage().getChatId();
            MeetingDto meetingDto = meetingStateCache.get(userId);

            if (meetingDto == null) {
                Optional<Meeting> optionalMeeting = meetingService.getLastChangedMeetingByOwnerId(userId);
                meetingDto = meetingConstructor.mapIfPresentOrElseGet(optionalMeeting, () -> new MeetingDto(userId));
            }
            if (!message.equals(AccountState.CREATE.getButtonName())) {
                handleStep(userId, meetingDto, message);
            }
            sendMessage(userId, meetingDto, message);
        }
    }

    protected void handleCallback(long userId, MeetingDto meetingDto, String message) {
        ToggleButton toggleButton = ToggleButton.typeOf(message);
        switch (toggleButton) {
            case SEND -> {
                Meeting meeting = meetingConstructor.mapToEntity(meetingDto);
                meetingService.saveByOwner(meeting);
                meetingDto.setId(meeting.getId());
                messageService.sendMeetingToParticipants(meetingDto);
                messageService.sendMessageSentSuccessfully(userId);
            }
            case NEXT -> {
                MeetingState currState = meetingDto.getState();
                MeetingState newState = MeetingState.setNextState(currState);
                meetingDto.setState(newState);
                sendMessage(userId, meetingDto, message);
            }
            case CANCEL -> {
//                meetingService.delete(meeting);
//                messageService.sendCanceledMessage(userId);
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
                    meetingDto.setGroupId(Integer.parseInt(callback));
                    meetingDto.setState(MeetingState.PARTICIPANT_SELECT);
                }
                case PARTICIPANT_SELECT -> meetingConstructor.updateParticipants(meetingDto, Long.parseLong(callback));
                case SUBJECT_SELECT -> {
                    meetingDto.setSubjectTitle(callback);
                    meetingDto.setState(MeetingState.SUBJECT_DURATION_SELECT);
                }
                case SUBJECT_DURATION_SELECT -> {
                    int duration = Integer.parseInt(callback);
                    meetingDto.setSubjectDuration(duration);
                    meetingDto.setState(MeetingState.QUESTION_SELECT);
                }
                case QUESTION_SELECT -> meetingConstructor.updateQuestion(meetingDto, callback);
                case DATE_SELECT -> meetingConstructor.updateDate(meetingDto, callback);
                case TIME_SELECT -> meetingConstructor.updateTime(meetingDto, callback);
                case ADDRESS_SELECT -> {
                    meetingDto.setState(MeetingState.ADDRESS_SELECT);
                }
            }
            meetingDto.setUpdatedDt(LocalDateTime.now());
            meetingStateCache.save(userId, meetingDto);
        } catch (NumberFormatException | DateTimeException ex) {
            log.debug("invalid value entered by user {}", userId);
        } finally {
//            meetingService.saveOnCache(userId, meetingDto);
        }
    }


    protected void sendMessage(long userId, MeetingDto meetingDto, String callback) {
        MeetingState state = meetingDto.getState();
        switch (state) {
            case GROUP_SELECT -> {
                messageService.sendGroupMessage(userId, meetingDto);
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
