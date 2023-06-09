package com.ufanet.meetingsbot.botapi.handlers.message;

import com.ufanet.meetingsbot.botapi.handlers.type.ChatType;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface MessageHandler {
    BotApiMethod<?> handleUpdate(Update update);
    ChatType getMessageType();
}
