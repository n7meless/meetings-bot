package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.state.AccountState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
@Component
public class UpcomingKeyboardHandler implements KeyboardHandler {
    @Override
    public void handleUpdate(Update update) {

    }

    @Override
    public AccountState getUserStateHandler() {
        return AccountState.UPCOMING;
    }
}
