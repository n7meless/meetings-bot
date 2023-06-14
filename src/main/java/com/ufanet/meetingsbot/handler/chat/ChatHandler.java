package com.ufanet.meetingsbot.handler.chat;

import com.ufanet.meetingsbot.handler.type.ChatType;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ChatHandler {
    void chatUpdate(Update update);
    ChatType getMessageType();
}
