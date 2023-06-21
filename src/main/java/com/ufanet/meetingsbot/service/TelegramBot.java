package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.config.BotConfig;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.handler.chat.ChatHandler;
import com.ufanet.meetingsbot.handler.type.ChatType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final Map<ChatType, ChatHandler> chatHandlers = new HashMap<>();
    private final AccountService accountService;
    private final UpdateService updateService;

    @Autowired
    public TelegramBot(BotConfig botConfig, List<ChatHandler> chatHandlers,
                       SetMyCommands setMyCommands, AccountService accountService, UpdateService updateService) {
        this.botConfig = botConfig;
        this.accountService = accountService;
        this.updateService = updateService;
        chatHandlers.forEach(handler -> this.chatHandlers.put(handler.getMessageType(), handler));
        safeExecute(setMyCommands);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null) return;
        UpdateDto updateDto = updateService.parseUpdate(update);
        ChatType chat = ChatType.typeOf(updateDto.chatType());

        log.info("received message from {}", updateDto.chatId());

        switch (chat) {
            case PRIVATE -> chatHandlers.get(ChatType.PRIVATE).chatUpdate(update);
            case GROUP, SUPERGROUP -> chatHandlers.get(ChatType.GROUP).chatUpdate(update);
        }
    }

    public <T extends Serializable, Method extends BotApiMethod<T>> T safeExecute(BotApiMethod<?> message) {
        try {
            return (T) execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error("При отправке на сервер телеграмма произошла ошибка!");
            return null;
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

}