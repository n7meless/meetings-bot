package com.ufanet.meetingsbot.handler.event.impl;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.constants.state.PreviousState;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.entity.Meeting;
import com.ufanet.meetingsbot.exceptions.AccountNotFoundException;
import com.ufanet.meetingsbot.exceptions.MeetingNotFoundException;
import com.ufanet.meetingsbot.handler.event.EventHandler;
import com.ufanet.meetingsbot.mapper.AccountMapper;
import com.ufanet.meetingsbot.mapper.MeetingMapper;
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
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PreviousEventHandler implements EventHandler {
    private final MeetingService meetingService;
    private final UpcomingReplyMessage upcomingReplyMessage;
    private final BotService botService;
    private final AccountService accountService;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    private void handleCallback(CallbackQuery query) {
        long userId = query.getFrom().getId();
        String callback = query.getData();

        String[] split = callback.split(" ");
        PreviousState state = PreviousState.typeOf(split[0]);
        if (state == null) return;

        botService.setState(userId, state.name());
        if (split.length > 1) {
            handleCallbackWithParams(userId, split, state);
        } else {
            if (state == PreviousState.PREVIOUS_MEETINGS) {
                handleReplyButton(userId);
            }
        }
    }

    private void handleCallbackWithParams(long userId, String[] callback, PreviousState state) {
        long meetingId = Long.parseLong(callback[1]);
        if (Objects.requireNonNull(state) == PreviousState.PREVIOUS_MEETINGS) {
            Optional<Meeting> optionalMeeting = meetingService.getByMeetingId(meetingId);
            MeetingDto meetingDto = MeetingMapper.MAPPER.mapIfPresentOrElseThrow(optionalMeeting,
                    () -> new MeetingNotFoundException(userId));
            upcomingReplyMessage.sendPassedMeetings(userId, meetingDto);
        }
    }

    private void handleReplyButton(long userId) {
        List<MeetingDto> meetingDtoList =
                meetingService.getMeetingsByUserIdAndStateIn(userId, List.of(MeetingState.PASSED))
                        .stream().map(MeetingMapper.MAPPER::mapWithMeetingDateAndTimes).toList();
        if (meetingDtoList.isEmpty()) {
            upcomingReplyMessage.sendPreviousMeetingsNotExists(userId);
        } else {
            AccountDto accountDto = accountService.getByUserId(userId).map(AccountMapper.MAPPER::mapWithSettings)
                    .orElseThrow(() -> new AccountNotFoundException(userId));
            upcomingReplyMessage.sendPreviousMeetingsList(userId, accountDto, meetingDtoList);
        }
    }

    private void handleMessage(Message message) {
        String messageText = message.getText();
        long userId = message.getChatId();
        if (messageText.equals(AccountState.PREVIOUS.getButtonName())) {
            handleReplyButton(userId);
        }
    }

    @Override
    public AccountState getAccountStateHandler() {
        return AccountState.PREVIOUS;
    }
}
