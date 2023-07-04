package com.ufanet.meetingsbot.handler.event.impl;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.dto.AccountTimeDto;
import com.ufanet.meetingsbot.dto.MeetingDateDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.MeetingTimeDto;
import com.ufanet.meetingsbot.entity.AccountTime;
import com.ufanet.meetingsbot.entity.Meeting;
import com.ufanet.meetingsbot.exceptions.AccountTimeNotFoundException;
import com.ufanet.meetingsbot.exceptions.MeetingNotFoundException;
import com.ufanet.meetingsbot.handler.event.EventHandler;
import com.ufanet.meetingsbot.mapper.AccountTimeMapper;
import com.ufanet.meetingsbot.mapper.MeetingMapper;
import com.ufanet.meetingsbot.mapper.MeetingTimeMapper;
import com.ufanet.meetingsbot.message.UpcomingReplyMessage;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ufanet.meetingsbot.constants.state.AccountState.UPCOMING;

@Component
@RequiredArgsConstructor
public class UpcomingEventHandler implements EventHandler {
    private final UpcomingReplyMessage replyMessage;
    private final MeetingService meetingService;
    private final AccountService accountService;
    private final BotService botService;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    private void handleMessage(Message message) {
        long userId = message.getChatId();
        String messageText = message.getText();
        if (messageText.equals(UPCOMING.getButtonName())) {
            meetingService.clearCache(userId);
            handleReplyButton(userId);
        }
    }

    private void handleReplyButton(long userId) {
        List<MeetingDto> meetings = meetingService.getMeetingsByUserIdAndStateIn(userId,
                        List.of(MeetingState.AWAITING, MeetingState.CONFIRMED))
                .stream().map(MeetingMapper.MAPPER::mapWithMeetingDateAndTimes).toList();

        if (meetings.isEmpty()) {
            replyMessage.sendMeetingsNotExist(userId);
        } else {
            replyMessage.sendUpcomingMeetingsList(userId, meetings);
        }
    }

    private void handleCallback(CallbackQuery query) {
        long userId = query.getFrom().getId();
        String callback = query.getData();

        String[] split = callback.split(" ");
        UpcomingState state = UpcomingState.typeOf(split[0]);
        if (state == null) return;

        botService.setState(userId, state.name());

        if (split.length > 1) {
            handleCallbackWithParams(userId, split, state);
        } else {
            if (state == UpcomingState.UPCOMING_MEETINGS) {
                handleReplyButton(userId);
            }
        }
    }

    private void handleCallbackWithParams(long userId, String[] callback, UpcomingState state) {
        long meetingId = Long.parseLong(callback[1]);

        Optional<Meeting> meeting = meetingService.getFromCache(userId);

        if (meeting.isEmpty()) {
            meeting = meetingService.getByMeetingId(meetingId);
        }

        MeetingDto meetingDto = MeetingMapper.MAPPER.mapIfPresentOrElseThrow(meeting,
                () -> new MeetingNotFoundException(userId));

        switch (state) {
            case UPCOMING_CANCEL_BY_OWNER -> {
                meetingService.deleteById(meetingId);
                replyMessage.sendCanceledMeetingByOwner(userId, meetingDto);
            }
            case UPCOMING_SELECTED_MEETING -> {

                List<AccountTimeDto> accountTimes = accountService.getAccountTimesByMeetingId(meetingId)
                        .stream().map(AccountTimeMapper.MAPPER::map).toList();

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
                List<AccountTimeDto> accountTimeDtos = accountService.getAccountTimesByMeetingId(meetingId)
                        .stream().filter(accountTime -> accountTime.getAccount().getId() == userId)
                        .map(AccountTimeMapper.MAPPER::map).toList();

                if (callback.length > 2) {

                    long accountTimeId = Long.parseLong(callback[2]);
                    AccountTimeDto accountTimeDto = accountTimeDtos.stream()
                            .filter(at -> at.getId() == accountTimeId).findFirst()
                            .orElseThrow(() -> new AccountTimeNotFoundException(userId));

                    Status status = accountTimeDto.getStatus();

                    switch (status) {
                        case CONFIRMED, AWAITING -> accountTimeDto.setStatus(Status.CANCELED);
                        case CANCELED -> accountTimeDto.setStatus(Status.CONFIRMED);
                    }
                    AccountTime accountTime = AccountTimeMapper.MAPPER.map(accountTimeDto);
                    accountService.saveAccountTime(accountTime);
                    meetingService.clearCache(userId);
                }
                replyMessage.sendEditMeetingAccountTimes(userId, meetingDto, accountTimeDtos);
            }
            case UPCOMING_CANCEL_MEETING_TIME -> {
                meetingDto.getDates().clear();
                meetingDto.setState(MeetingState.CANCELED);
                Meeting entity = MeetingMapper.MAPPER.mapToFullEntity(meetingDto);
                meetingService.save(entity);
                replyMessage.sendCanceledAccountTimeMessage(meetingDto);
                meetingService.clearCache(userId);
            }
            case UPCOMING_CONFIRM_MEETING_TIME -> {
                List<AccountTimeDto> accountTimeDtos = accountService.getAccountTimesByMeetingId(meetingId)
                        .stream().map(AccountTimeMapper.MAPPER::map).toList();

                for (AccountTimeDto accountTimeDto : accountTimeDtos) {
                    Status status = accountTimeDto.getStatus();
                    if (status.equals(Status.AWAITING) &&
                            accountTimeDto.getAccount().getId() == userId) {

                        accountTimeDto.setStatus(Status.CONFIRMED);
                    }
                }
                List<AccountTime> accountTimes = accountTimeDtos.stream()
                        .filter(at -> at.getAccount().getId() == userId)
                        .map(AccountTimeMapper.MAPPER::map).toList();

                accountService.saveAccountTimes(accountTimes);
                handleAccountTimeConfirm(userId, meetingDto, accountTimeDtos);
            }
            case UPCOMING_IWILLNOTCOME, UPCOMING_IAMLATE, UPCOMING_IAMREADY, UPCOMING_IAMCONFIRM ->
                    handleAccountTimeState(userId, meetingDto, state);
        }
    }

    private void handleAccountTimeConfirm(long userId, MeetingDto meetingDto,
                                          List<AccountTimeDto> accountTimeDtos) {

        boolean allVoted = accountTimeDtos.stream()
                .allMatch(at -> at.getStatus().equals(Status.CONFIRMED) ||
                        at.getStatus().equals(Status.CANCELED));

        if (allVoted) {
            Optional<MeetingTimeDto> confirmedMeetingTime =
                    meetingService.getByMeetingIdAndConfirmedState(meetingDto.getId())
                            .map(MeetingTimeMapper.MAPPER::map);

            if (confirmedMeetingTime.isPresent()) {
                MeetingTimeDto meetingTimeDto = confirmedMeetingTime.get();

                Set<AccountTimeDto> accountTimeDtoSet = accountTimeDtos.stream()
                        .filter(t -> t.getStatus().equals(Status.CONFIRMED) &&
                                t.getMeetingTime().getId().equals(meetingTimeDto.getId()))
                        .collect(Collectors.toSet());

                MeetingDateDto meetingDate = meetingTimeDto.getMeetingDate();

                meetingTimeDto.setAccountTimes(accountTimeDtoSet);
                meetingDate.setMeetingTimes(Set.of(meetingTimeDto));
                meetingDto.setDates(Set.of(meetingDate));

                meetingDto.setState(MeetingState.CONFIRMED);
                Meeting meeting = MeetingMapper.MAPPER.mapToFullEntity(meetingDto);
                meetingService.save(meeting);

                replyMessage.sendReadyMeeting(meetingDto);
            } else {

                meetingDto.removeDateIf(md -> true);
                meetingDto.setState(MeetingState.CANCELED);
                Meeting meeting = MeetingMapper.MAPPER.mapToFullEntity(meetingDto);
                meetingService.save(meeting);

                replyMessage.sendCanceledMeetingByMatching(meetingDto);
            }

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
        AccountTime mapped = AccountTimeMapper.MAPPER.map(accountTimeDto);
        accountService.saveAccountTime(mapped);
        replyMessage.sendSelectedUpcomingMeeting(userId, meetingDto, accountTimes);
//        Meeting meeting = MeetingMapper.Mapper.map(meetingDto);
        meetingService.clearCache(userId);
    }

    @Override
    public AccountState getAccountStateHandler() {
        return UPCOMING;
    }
}
