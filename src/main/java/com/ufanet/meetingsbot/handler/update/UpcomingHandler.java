package com.ufanet.meetingsbot.handler.update;

import com.ufanet.meetingsbot.handler.type.HandlerType;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
@Component
public class UpcomingHandler implements UpdateHandler {
    @Override
    public BotApiMethod<?> handleUpdate(Update update) {
        return null;
    }

    @Override
    public HandlerType getTypeHandler() {
        return HandlerType.UPCOMING;
    }
}
