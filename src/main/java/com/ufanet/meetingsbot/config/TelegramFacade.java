package com.ufanet.meetingsbot.config;

import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.handler.chat.ChatHandler;
import com.ufanet.meetingsbot.handler.type.ChatType;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.UpdateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramFacade {

    private Map<ChatType, ChatHandler> chatHandlers = new HashMap<>();
    private final AccountService accountService;
    private final UpdateService updateService;

    @Autowired
    public TelegramFacade(List<ChatHandler> chatHandlers, AccountService accountService,
                          UpdateService updateService) {
        this.accountService = accountService;
        this.updateService = updateService;
        chatHandlers.forEach(handler -> this.chatHandlers.put(handler.getChatType(), handler));
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        UpdateDto updateDto = updateService.parseUpdate(update);
        ChatType chat = ChatType.typeOf(updateDto.chatType());
        if (chat == null) return null;

        log.info("received message from {}", updateDto.chatId());

        switch (chat) {
            case PRIVATE, SENDER -> chatHandlers.get(ChatType.PRIVATE).chatUpdate(update);
            case GROUP, SUPERGROUP -> chatHandlers.get(ChatType.GROUP).chatUpdate(update);
        }
        return null;
    }
}
