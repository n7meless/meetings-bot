package com.ufanet.meetingsbot.handler.update;

import com.ufanet.meetingsbot.handler.type.HandlerType;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {
    BotApiMethod<?> handleUpdate(Update update);
    HandlerType getTypeHandler();
}
