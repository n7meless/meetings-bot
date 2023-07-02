package com.ufanet.meetingsbot.handler.event.impl;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.dto.AccountTimeDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.exceptions.AccountTimeNotFoundException;
import com.ufanet.meetingsbot.exceptions.MeetingNotFoundException;
import com.ufanet.meetingsbot.handler.event.EventHandler;
import com.ufanet.meetingsbot.mapper.AccountTimeMapper;
import com.ufanet.meetingsbot.mapper.MeetingMapper;
import com.ufanet.meetingsbot.message.UpcomingReplyMessage;
import com.ufanet.meetingsbot.model.AccountTime;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.model.MeetingTime;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.MeetingConstructor;
import com.ufanet.meetingsbot.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

import static com.ufanet.meetingsbot.constants.state.AccountState.UPCOMING;

@Component
@RequiredArgsConstructor
public class UpcomingEventHandler implements EventHandler {
    private final UpcomingReplyMessage replyMessage;
    private final MeetingService meetingService;
    private final AccountService accountService;
    private final BotService botService;
    private final MeetingConstructor meetingConstructor;
    private final MeetingMapper meetingMapper;
    private final AccountTimeMapper accountTimeMapper;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasMessage()) {
            long userId = update.getMessage().getChatId();
            String message = update.getMessage().getText();
            if (message.equals(UPCOMING.getButtonName())) {
                meetingService.clearCache(userId);
                handleCallback(userId, UpcomingState.UPCOMING_MEETINGS.name());
            }
        } else if (update.hasCallbackQuery()) {
            long userId = update.getCallbackQuery().getFrom().getId();
            String callback = update.getCallbackQuery().getData();
            handleCallback(userId, callback);
        }
    }

    private void handleCallback(long userId, String callback) {
        String[] split = callback.split(" ");
        UpcomingState state = UpcomingState.typeOf(split[0]);
        if (state == null) return;

        botService.setState(userId, state.name());

        if (split.length > 1) {
            handleCallbackWithParams(userId, split, state);
        } else {
            //TODO map to dto
            if (state == UpcomingState.UPCOMING_MEETINGS) {
                List<MeetingDto> meetings = meetingService.getMeetingsByUserIdAndStateIn(userId,
                                List.of(MeetingState.AWAITING, MeetingState.CONFIRMED))
                        .stream().map(meetingMapper::map).toList();

                if (meetings.isEmpty()) {
                    replyMessage.sendMeetingsNotExist(userId);
                } else {
                    replyMessage.sendUpcomingMeetings(userId, meetings);
                }
            }
        }
    }

    private void handleCallbackWithParams(long userId, String[] callback, UpcomingState state) {
        long meetingId = Long.parseLong(callback[1]);

        Optional<Meeting> optionalMeeting = meetingService.getFromCache(userId);

        if (optionalMeeting.isEmpty()) {
            optionalMeeting = meetingService.getByMeetingId(meetingId);
        }

        MeetingDto meetingDto = meetingMapper.mapIfPresentOrElseThrow(optionalMeeting,
                () -> new MeetingNotFoundException(userId));

        switch (state) {
            case UPCOMING_CANCEL_BY_OWNER -> {
                meetingService.deleteById(meetingId);
                replyMessage.sendCanceledMeetingByOwner(userId, meetingDto);
            }
            case UPCOMING_SELECTED_MEETING -> {

                List<AccountTimeDto> accountTimes = accountService.getAccountTimesByMeetingId(meetingId)
                        .stream().map(accountTimeMapper::map).toList();

                if (meetingDto.getState().equals(MeetingState.AWAITING)) {
                    if (meetingDto.getOwner().getId() == userId) {
                        replyMessage.sendSelectedReadyMeeting(userId, meetingDto);
                    } else {
                        replyMessage.sendSelectedAwaitingMeeting(userId, meetingDto);
                    }
                } else replyMessage.sendSelectedUpcomingMeeting(userId, meetingDto, accountTimes);
            }
            case UPCOMING_SELECT_PARTICIPANT -> replyMessage.sendParticipantSelectionForPing(userId, meetingDto);
            case UPCOMING_SEND_NOTIFICATION_PARTICIPANT -> {
                long participantId = Long.parseLong(callback[2]);
                replyMessage.sendPingParticipant(userId, participantId, meetingDto);
            }
            case UPCOMING_EDIT_MEETING_TIME -> {
                List<AccountTimeDto> accountTimes = accountService.getAccountTimesByMeetingId(meetingId)
                        .stream().filter(accountTime -> accountTime.getAccount().getId() == userId)
                        .map(accountTimeMapper::map).toList();

                if (callback.length > 2) {

                    long accountTimeId = Long.parseLong(callback[2]);
                    AccountTimeDto accountTimeDto = accountTimes.stream()
                            .filter(at -> at.getId() == accountTimeId).findFirst()
                            .orElseThrow(() -> new AccountTimeNotFoundException(userId));

                    Status status = accountTimeDto.getStatus();

                    switch (status) {
                        case CONFIRMED, AWAITING -> accountTimeDto.setStatus(Status.CANCELED);
                        case CANCELED -> accountTimeDto.setStatus(Status.CONFIRMED);
                    }
                    AccountTime accountTime = accountTimeMapper.map(accountTimeDto);
                    accountService.saveAccountTime(accountTime);
                    meetingService.clearCache(userId);
                }
                replyMessage.sendEditMeetingAccountTimes(userId, meetingDto, accountTimes);
            }
            case UPCOMING_CANCEL_MEETING_TIME -> {
                meetingDto.getDates().clear();
                Meeting meeting = meetingMapper.map(meetingDto);
                meetingService.save(meeting);
                replyMessage.sendCanceledAccountTimeMessage(meetingDto);
                meetingService.clearCache(userId);
            }
            case UPCOMING_CONFIRM_MEETING_TIME -> {
                List<AccountTime> accountTimes = accountService.getAccountTimesByMeetingId(meetingId);

                for (AccountTime accountTime : accountTimes) {
                    Status status = accountTime.getStatus();
                    if (status.equals(Status.AWAITING) &&
                            accountTime.getAccount().getId() == userId) {

                        accountTime.setStatus(Status.CONFIRMED);
                    }
                }
                accountService.saveAccountTimes(accountTimes);
                handleAccountTimeConfirm(userId, meetingDto, accountTimes);
            }
            case UPCOMING_IWILLNOTCOME, UPCOMING_IAMLATE, UPCOMING_IAMREADY, UPCOMING_IAMCONFIRM ->
                    handleAccountTimeState(userId, meetingDto, state);
        }
    }

    private void handleAccountTimeConfirm(long userId, MeetingDto meetingDto,
                                          List<AccountTime> accountTimes) {

        boolean allVoted = accountTimes.stream()
                .allMatch(at -> at.getStatus().equals(Status.CONFIRMED) ||
                        at.getStatus().equals(Status.CANCELED));

        Optional<MeetingTime> confirmedTime =
                meetingService.getByMeetingIdAndConfirmedState(meetingDto.getId());

        if (allVoted && confirmedTime.isPresent()) {
            MeetingTime meetingTime = confirmedTime.get();
            meetingConstructor.updateAccountTimes(meetingDto, meetingTime);

            meetingDto.setState(MeetingState.CONFIRMED);
            Meeting meeting = meetingMapper.map(meetingDto);
            meetingService.save(meeting);

            replyMessage.sendReadyMeeting(meetingDto);
        } else if (allVoted) {
            //TODO save the meeting for statistics in future?
            meetingDto.setState(MeetingState.CANCELED);
            Meeting meeting = meetingMapper.map(meetingDto);
            meetingService.save(meeting);

            replyMessage.sendCanceledMeetingByMatching(meetingDto);
        } else {
            replyMessage.sendSuccessMeetingConfirm(userId);
        }
    }

    private void handleAccountTimeState(long userId, MeetingDto meetingDto, UpcomingState state) {

        List<AccountTimeDto> accountTimes = meetingDto.getAccountTimes((a) -> true);
        AccountTimeDto accountTimeDto = accountTimes.stream()
                .filter(at -> at.getAccount().getId() == userId).findFirst().orElseThrow();

        switch (state) {
            case UPCOMING_IAMREADY -> accountTimeDto.setStatus(Status.READY);
            case UPCOMING_IAMLATE -> accountTimeDto.setStatus(Status.AWAITING);
            case UPCOMING_IWILLNOTCOME -> accountTimeDto.setStatus(Status.CANCELED);
            case UPCOMING_IAMCONFIRM -> accountTimeDto.setStatus(Status.CONFIRMED);
        }
        AccountTime mapped = accountTimeMapper.map(accountTimeDto);
        accountService.saveAccountTime(mapped);
        replyMessage.sendSelectedUpcomingMeeting(userId, meetingDto, accountTimes);
//        Meeting meeting = meetingMapper.map(meetingDto);
        meetingService.clearCache(userId);
    }

    @Override
    public AccountState getAccountStateHandler() {
        return UPCOMING;
    }
}
