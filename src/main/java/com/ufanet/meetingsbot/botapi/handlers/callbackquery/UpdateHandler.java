package com.ufanet.meetingsbot.botapi.handlers.callbackquery;

import com.ufanet.meetingsbot.botapi.handlers.type.HandlerType;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {
    BotApiMethod<?> handleUpdate(Update update);
    HandlerType getTypeHandler();
}
