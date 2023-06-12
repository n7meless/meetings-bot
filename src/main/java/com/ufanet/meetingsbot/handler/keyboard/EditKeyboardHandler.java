package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.state.AccountState;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@AllArgsConstructor
public class EditKeyboardHandler implements KeyboardHandler {

    @Override
    public BotApiMethod<?> handleUpdate(Update update) {
        return null;
    }

    @Override
    public AccountState getUserStateHandler() {
        return AccountState.UPDATE;
    }
}
