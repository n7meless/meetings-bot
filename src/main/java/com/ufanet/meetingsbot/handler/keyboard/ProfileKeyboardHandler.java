package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.state.AccountState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ProfileKeyboardHandler implements KeyboardHandler {

    @Override
    public BotApiMethod<?> handleUpdate(Update update) {
        return null;
    }

    @Override
    public AccountState getUserStateHandler() {
        return AccountState.PROFILE;
    }
}
