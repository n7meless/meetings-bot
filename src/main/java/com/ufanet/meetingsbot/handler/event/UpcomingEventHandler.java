package com.ufanet.meetingsbot.handler.event;

import com.ufanet.meetingsbot.cache.impl.MeetingStateCache;
import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.mapper.MeetingConstructor;
import com.ufanet.meetingsbot.model.AccountTime;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.model.MeetingTime;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.message.UpcomingReplyMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

import static com.ufanet.meetingsbot.constants.state.AccountState.UPCOMING;

@Component
@RequiredArgsConstructor
public class UpcomingEventHandler implements EventHandler {
    private final UpcomingReplyMessageService replyMessage;
    private final MeetingService meetingService;
    private final AccountService accountService;
    private final BotService botService;
    private final MeetingStateCache meetingStateCache;
    private final MeetingConstructor meetingConstructor;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasMessage()) {
            long userId = update.getMessage().getChatId();
            String message = update.getMessage().getText();
            if (message.equals(UPCOMING.getButtonName())) {
                handleCallback(userId, UpcomingState.UPCOMING_MEETINGS.name());
            }
        } else if (update.hasCallbackQuery()) {
            long userId = update.getCallbackQuery().getMessage().getChatId();
            String callback = update.getCallbackQuery().getData();
            handleCallback(userId, callback);
        }
    }

    protected void handleCallback(long userId, String callback) {
        String[] split = callback.split(" ");
        UpcomingState state = UpcomingState.typeOf(split[0]);
        if (state == null) return;
        if (split.length > 1) {
            long meetingId = Long.parseLong(split[1]);

            MeetingDto meetingDto = meetingStateCache.get(userId);

            if (meetingDto == null) {
                Optional<Meeting> optionalMeeting = meetingService.getByMeetingId(meetingId);
                meetingDto = meetingConstructor.mapIfPresentOrElseThrow(optionalMeeting,
                        RuntimeException::new);
            }

            switch (state) {
                case UPCOMING_CANCEL_BY_OWNER -> {
                    replyMessage.sendCanceledMeetingByOwner(userId, meetingDto);
                    meetingService.deleteById(meetingId);
                }
                case UPCOMING_SELECTED_MEETING -> {
                    List<AccountTime> accountTimes = accountService.getAccountTimesByMeetingId(meetingId);
                    if (meetingDto.getState().equals(MeetingState.AWAITING)) {
                        if (meetingDto.getOwner().getId() == userId) {
                            replyMessage.sendSelectedReadyMeeting(userId, meetingDto);
                        } else {
                            replyMessage.sendSelectedAwaitingMeeting(userId, meetingDto);
                        }
                    } else replyMessage.sendSelectedUpcomingMeeting(userId, meetingDto, accountTimes);
                }
                case UPCOMING_EDIT_MEETING_TIME -> {
                    List<AccountTime> accountTimes = accountService.getAccountTimesByUserIdAndMeetingId(userId, meetingId);

                    if (split.length > 2) {
                        long accTimeId = Long.parseLong(split[2]);
                        accountService.updateMeetingAccountTime(accTimeId, accountTimes);
//                        meetingService.saveOnCache(userId, meeting);
                    }
                    replyMessage.sendEditMeetingAccountTimes(userId, meetingDto, accountTimes);
                }
                case UPCOMING_CANCEL_MEETING_TIME -> {
                    meetingDto.getDatesMap().clear();
//                    meetingService.cancelMeeting(meetingDto);
//                meetingService.saveOnCache(userId, meeting);
                    replyMessage.sendCanceledAccountTimeMessage(meetingDto);
                }
                case UPCOMING_CONFIRM_MEETING_TIME -> {
                    List<AccountTime> accountTimes = accountService.getAccountTimesByMeetingId(meetingId);
                    meetingService.updateMeetingAccountTimes(userId, accountTimes);

                    boolean allVoted = accountTimes.stream()
                            .allMatch(at -> at.getStatus().equals(Status.CONFIRMED) ||
                                    at.getStatus().equals(Status.CANCELED));

                    Optional<MeetingTime> confirmed = meetingService.getByMeetingIdAndConfirmedState(meetingId);

                    if (allVoted && confirmed.isPresent()) {
                        Map<LocalDate, Set<ZonedDateTime>> datesMap = meetingDto.getDatesMap();
                        datesMap.clear();
                        MeetingTime meetingTime = confirmed.get();
                        ZonedDateTime dateTime = meetingTime.getDateTime();
                        datesMap.put(dateTime.toLocalDate(), Set.of(dateTime));

                        meetingDto.setState(MeetingState.CONFIRMED);
                        meetingDto.setDatesMap(datesMap);
//                        meetingService.processConfirmedMeeting(userId, meetingDto, confirmed);
//                    meetingService.clearCache(userId);
                        replyMessage.sendReadyMeeting(meetingDto);
                    } else if (allVoted) {
                        meetingDto.setDatesMap(new TreeMap<>());
                        Meeting meeting = meetingConstructor.mapToEntity(meetingDto);
                        meetingService.cancelMeeting(meeting);
//                    meetingService.clearCache(userId);
                        replyMessage.sendCanceledMeetingByMatching(meetingDto);
                    } else {
                        replyMessage.sendSuccessMeetingConfirm(userId);
                    }
                }
                case UPCOMING_IWILLNOTCOME, UPCOMING_IAMLATE, UPCOMING_IAMREADY -> {
                    handleAccountTimeState(userId, meetingId, state);
                }
            }
        }
        botService.setState(userId, state.name());

        switch (state) {
            case UPCOMING_MEETINGS -> {
                List<Meeting> meetings = meetingService.getMeetingsByUserIdAndStateIn(userId,
                        List.of(MeetingState.AWAITING, MeetingState.CONFIRMED));

                if (meetings.isEmpty()) {
                    replyMessage.sendMeetingsNotExist(userId);
                } else {
                    replyMessage.sendUpcomingMeetings(userId, meetings);
                }
            }
        }
    }

    protected void handleAccountTimeState(long userId, long meetingId, UpcomingState state) {
        MeetingDto meetingDto = meetingStateCache.get(userId);

        if (meetingDto == null) {
            Optional<Meeting> optionalMeeting = meetingService.getByMeetingId(meetingId);
            meetingDto = meetingConstructor.mapIfPresentOrElseThrow(optionalMeeting,
                    RuntimeException::new);
        }

        List<AccountTime> accountTimes = accountService.getAccountTimesByMeetingId(meetingId);

        AccountTime accountTime = accountTimes.stream()
                .filter(time -> time.getAccount().getId() == userId)
                .findFirst().orElseThrow();

        switch (state) {
            case UPCOMING_IAMREADY -> {
                accountTime.setStatus(Status.CONFIRMED);
                accountService.saveAccountTime(accountTime);
                replyMessage.sendSelectedUpcomingMeeting(userId, meetingDto, accountTimes);
            }
            case UPCOMING_IAMLATE -> {
                accountTime.setStatus(Status.AWAITING);
                accountService.saveAccountTime(accountTime);
                replyMessage.sendSelectedUpcomingMeeting(userId, meetingDto, accountTimes);
            }
            case UPCOMING_IWILLNOTCOME -> {
                accountTime.setStatus(Status.CANCELED);
                accountService.saveAccountTime(accountTime);
                replyMessage.sendSelectedUpcomingMeeting(userId, meetingDto, accountTimes);
            }
        }
        meetingService.clearCache(userId);
    }

    @Override
    public AccountState getAccountStateHandler() {
        return UPCOMING;
    }
}
