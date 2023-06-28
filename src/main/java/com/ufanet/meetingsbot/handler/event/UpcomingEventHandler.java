package com.ufanet.meetingsbot.handler.event;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
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

import java.util.List;
import java.util.Optional;

import static com.ufanet.meetingsbot.constants.state.AccountState.UPCOMING;

@Component
@RequiredArgsConstructor
public class UpcomingEventHandler implements EventHandler {
    private final UpcomingReplyMessageService replyMessage;
    private final MeetingService meetingService;
    private final AccountService accountService;
    private final BotService botService;

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
            case UPCOMING_CANCEL_BY_OWNER -> {
                long meetingId = Long.parseLong(split[1]);
                Meeting meeting = meetingService.getByMeetingId(userId, meetingId);

                replyMessage.sendCanceledMeetingByOwner(userId, meeting);
                meetingService.delete(meeting);
            }
            case UPCOMING_SELECTED_MEETING -> {
                long meetingId = Long.parseLong(split[1]);
                Meeting meeting = meetingService.getByMeetingId(userId, meetingId);
                List<AccountTime> accountTimes = accountService.getAccountTimesByMeetingId(meetingId);
                if (meeting.getState().equals(MeetingState.AWAITING)) {
                    if (meeting.getOwner().getId() == userId) {
                        replyMessage.sendSelectedReadyMeeting(userId, meeting);
                    } else {
                        replyMessage.sendSelectedAwaitingMeeting(userId, meeting);
                    }
                } else replyMessage.sendSelectedUpcomingMeeting(userId, meeting, accountTimes);
            }
            case UPCOMING_EDIT_MEETING_TIME -> {
                long meetingId = Long.parseLong(split[1]);
                Meeting meeting = meetingService.getByMeetingId(userId, meetingId);
                List<AccountTime> accountTimes = meeting.getAccountTimes(at -> at.getAccount().getId() == userId);

                if (split.length > 2) {
                    long accTimeId = Long.parseLong(split[2]);
                    meetingService.updateMeetingAccountTime(meeting, accTimeId, accountTimes);
                    meetingService.saveOnCache(userId, meeting);
                }
                replyMessage.sendEditMeetingAccountTimes(userId, meeting, accountTimes);
            }
            case UPCOMING_CANCEL_MEETING_TIME -> {
                long meetingId = Long.parseLong(split[1]);
                Meeting meeting = meetingService.getByMeetingId(userId, meetingId);
                meetingService.cancelMeeting(meeting);
//                meetingService.saveOnCache(userId, meeting);
                replyMessage.sendCanceledAccountTimeMessage(meeting);
            }
            case UPCOMING_CONFIRM_MEETING_TIME -> {
                long meetingId = Long.parseLong(split[1]);
                Meeting meeting = meetingService.getByMeetingId(userId, meetingId);

                List<AccountTime> accountTimes = accountService.getAccountTimesByMeetingId(meetingId);
                meetingService.updateMeetingAccountTimes(userId, accountTimes);

                boolean allVoted = accountTimes.stream()
                        .allMatch(at -> at.getStatus().equals(Status.CONFIRMED) ||
                                at.getStatus().equals(Status.CANCELED));

                Optional<MeetingTime> confirmed = meetingService.getByMeetingIdAndConfirmedState(meetingId);

                if (allVoted && confirmed.isPresent()) {
                    meetingService.processConfirmedMeeting(userId, meeting, confirmed);
//                    meetingService.clearCache(userId);
                    replyMessage.sendReadyMeeting(meeting);
                } else if (allVoted) {
                    meetingService.cancelMeeting(meeting);
//                    meetingService.clearCache(userId);
                    replyMessage.sendCanceledMeetingByMatching(meeting);
                } else {
                    replyMessage.sendSuccessMeetingConfirm(userId);
                }
            }
            case UPCOMING_IWILLNOTCOME, UPCOMING_IAMLATE, UPCOMING_IAMREADY -> {
                long meetingId = Long.parseLong(split[1]);
                handleAccountTimeState(userId, meetingId, state);
            }
        }
    }

    protected void handleAccountTimeState(long userId, long meetingId, UpcomingState state) {
        Meeting meeting = meetingService.getByMeetingId(userId, meetingId);
        List<AccountTime> accountTimes = accountService.getAccountTimesByMeetingId(meetingId);

        AccountTime accountTime = accountTimes.stream()
                .filter(time -> time.getAccount().getId() == userId)
                .findFirst().orElseThrow();

        switch (state) {
            case UPCOMING_IAMREADY -> {
                accountTime.setStatus(Status.CONFIRMED);
                accountService.saveAccountTime(accountTime);
                replyMessage.sendSelectedUpcomingMeeting(userId, meeting, accountTimes);
            }
            case UPCOMING_IAMLATE -> {
                accountTime.setStatus(Status.AWAITING);
                accountService.saveAccountTime(accountTime);
                replyMessage.sendSelectedUpcomingMeeting(userId, meeting, accountTimes);
            }
            case UPCOMING_IWILLNOTCOME -> {
                accountTime.setStatus(Status.CANCELED);
                accountService.saveAccountTime(accountTime);
                replyMessage.sendSelectedUpcomingMeeting(userId, meeting, accountTimes);
            }
        }
        meetingService.clearCache(userId);
    }

    @Override
    public AccountState getAccountStateHandler() {
        return UPCOMING;
    }
}