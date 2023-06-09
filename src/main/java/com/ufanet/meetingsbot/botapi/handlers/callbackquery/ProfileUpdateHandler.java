package com.ufanet.meetingsbot.botapi.handlers.callbackquery;

import com.ufanet.meetingsbot.botapi.handlers.type.HandlerType;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ProfileUpdateHandler implements UpdateHandler {

    @Override
    public BotApiMethod<?> handleUpdate(Update update) {
        return null;
    }

    @Override
    public HandlerType getTypeHandler() {
        return HandlerType.PROFILE;
    }
}
