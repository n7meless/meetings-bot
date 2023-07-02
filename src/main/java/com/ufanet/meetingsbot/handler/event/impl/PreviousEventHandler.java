package com.ufanet.meetingsbot.handler.event.impl;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.handler.event.EventHandler;
import com.ufanet.meetingsbot.mapper.MeetingMapper;
import com.ufanet.meetingsbot.message.UpcomingReplyMessage;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PreviousEventHandler implements EventHandler {
    private final MeetingService meetingService;
    private final AccountService accountService;
    private final MeetingMapper meetingMapper;
    private final UpcomingReplyMessage upcomingReplyMessage;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getFrom().getId();

    }

    private void handleMessage(Message message) {
        String messageText = message.getText();
        long userId = message.getChatId();
        if (messageText.equals(AccountState.PREVIOUS.getButtonName())) {
            List<MeetingDto> meetingDtoList = meetingService.getMeetingsByUserIdAndStateIn(userId, List.of(MeetingState.PASSED))
                    .stream().map(meetingMapper::map).toList();
            upcomingReplyMessage.sendPreviousMeetingsList(userId, meetingDtoList);
        }
    }

    @Override
    public AccountState getAccountStateHandler() {
        return AccountState.PREVIOUS;
    }
}
