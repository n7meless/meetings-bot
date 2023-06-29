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

        botService.setState(userId, state.name());

        if (split.length > 1) {
            handleCallbackWithParams(userId, split, state);
        } else {
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
    }

    protected void handleCallbackWithParams(long userId, String[] callback, UpcomingState state) {
        long meetingId = Long.parseLong(callback[1]);

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
            case UPCOMING_SELECT_PARTICIPANT -> {
                replyMessage.sendParticipantSelectionForPing(userId, meetingDto);
            }
            case UPCOMING_SEND_NOTIFICATION_PARTICIPANT -> {
                long participantId = Long.parseLong(callback[2]);
                replyMessage.sendPingParticipant(userId, participantId, meetingDto);
            }
            case UPCOMING_EDIT_MEETING_TIME -> {
                List<AccountTime> accountTimes = accountService.getAccountTimesByMeetingId(meetingId)
                        .stream().filter(accountTime -> accountTime.getAccount().getId() == userId).toList();

                if (callback.length > 2) {
                    long accountTimeId = Long.parseLong(callback[2]);
                    accountService.updateMeetingAccountTime(accountTimeId, accountTimes);
//                    meetingStateCache.evict(userId);
                }
                replyMessage.sendEditMeetingAccountTimes(userId, meetingDto, accountTimes);
            }
            case UPCOMING_CANCEL_MEETING_TIME -> {
                meetingDto.getDates().clear();
                replyMessage.sendCanceledAccountTimeMessage(meetingDto);
                meetingStateCache.evict(userId);
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
            case UPCOMING_IWILLNOTCOME, UPCOMING_IAMLATE, UPCOMING_IAMREADY -> {
                handleAccountTimeState(userId, meetingId, state);
            }
        }
    }

    protected void handleAccountTimeConfirm(long userId, MeetingDto meetingDto,
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
            Meeting meeting = meetingConstructor.mapToEntity(meetingDto);
            meetingService.save(meeting);

            replyMessage.sendReadyMeeting(meetingDto);
        } else if (allVoted) {
            //TODO save the meeting for statistics in future?
            meetingDto.setState(MeetingState.CANCELED);
            Meeting meeting = meetingConstructor.mapToEntity(meetingDto);
            meetingService.save(meeting);

            replyMessage.sendCanceledMeetingByMatching(meetingDto);
        } else {
            replyMessage.sendSuccessMeetingConfirm(userId);
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
            case UPCOMING_IAMREADY -> accountTime.setStatus(Status.CONFIRMED);
            case UPCOMING_IAMLATE -> accountTime.setStatus(Status.AWAITING);
            case UPCOMING_IWILLNOTCOME -> accountTime.setStatus(Status.CANCELED);
        }
        accountService.saveAccountTime(accountTime);
        replyMessage.sendSelectedUpcomingMeeting(userId, meetingDto, accountTimes);
        meetingStateCache.evict(userId);
    }

    @Override
    public AccountState getAccountStateHandler() {
        return UPCOMING;
    }
}
