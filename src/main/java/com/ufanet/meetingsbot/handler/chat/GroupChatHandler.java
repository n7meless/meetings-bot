package com.ufanet.meetingsbot.handler.chat;

import com.ufanet.meetingsbot.handler.type.ChatType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class GroupChatHandler implements ChatHandler {

    @Override
    public BotApiMethod<?> handleUpdate(Update update) {
        return null;
    }

    @Override
    public ChatType getMessageType() {
        return ChatType.GROUP;
    }
}
