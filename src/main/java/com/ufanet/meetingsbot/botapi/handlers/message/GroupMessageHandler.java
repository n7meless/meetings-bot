package com.ufanet.meetingsbot.botapi.handlers.message;

import com.ufanet.meetingsbot.botapi.handlers.type.ChatType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class GroupMessageHandler implements MessageHandler {

    @Override
    public BotApiMethod<?> handleUpdate(Update update) {
        return null;
    }

    @Override
    public ChatType getMessageType() {
        return ChatType.GROUP;
    }
}
