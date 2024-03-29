package com.ufanet.meetingsbot.handler.chat;

import com.ufanet.meetingsbot.constants.type.ChatType;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ChatHandler {
    void handleChatUpdate(Update update);

    ChatType getChatType();
}
