package com.ufanet.meetingsbot.handler.event.impl;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.handler.event.EventHandler;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PreviousEventHandler implements EventHandler {
    private final MeetingService meetingService;
    private final AccountService accountService;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasMessage()) {
            String message = update.getMessage().getText();
            long userId = update.getMessage().getChatId();
            if (message.equals(AccountState.PREVIOUS.getButtonName())) {
                List<Meeting> passedMeetings = meetingService.getMeetingsByUserIdAndStateIn(userId, List.of(MeetingState.PASSED));

            }
        }
    }

    @Override
    public AccountState getAccountStateHandler() {
        return AccountState.PREVIOUS;
    }
}
