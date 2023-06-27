package com.ufanet.meetingsbot.config;

import com.ufanet.meetingsbot.handler.chat.ChatHandler;
import com.ufanet.meetingsbot.handler.type.ChatType;
import com.ufanet.meetingsbot.service.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramFacade {

    private final Map<ChatType, ChatHandler> chatHandlers = new HashMap<>();
    private final AccountService accountService;

    @Autowired
    public TelegramFacade(List<ChatHandler> chatHandlers, AccountService accountService) {
        this.accountService = accountService;
        chatHandlers.forEach(handler -> this.chatHandlers.put(handler.getChatType(), handler));
    }

    public void handleUpdate(Update update) {
        String chatType = getChatType(update);
        ChatType chat = ChatType.typeOf(chatType);
        if (chat == null) return;

        switch (chat) {
            case PRIVATE, SENDER -> chatHandlers.get(ChatType.PRIVATE).chatUpdate(update);
            case GROUP, SUPERGROUP -> chatHandlers.get(ChatType.GROUP).chatUpdate(update);
        }
    }

    private String getChatType(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            return message.getChat().getType();
        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            Message message = query.getMessage();
            return message.getChat().getType();
        } else if (update.hasInlineQuery()) {
            return update.getInlineQuery().getChatType();
        } else return null;
    }
}
