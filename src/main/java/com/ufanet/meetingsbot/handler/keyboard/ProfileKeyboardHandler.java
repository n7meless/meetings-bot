package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.state.AccountState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ProfileKeyboardHandler implements KeyboardHandler {

    @Override
    public void handleUpdate(Update update) {

    }

    @Override
    public AccountState getAccountStateHandler() {
        return AccountState.PROFILE_SETTINGS;
    }
}
