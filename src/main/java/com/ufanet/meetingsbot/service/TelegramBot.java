package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.config.BotConfig;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.handler.chat.ChatHandler;
import com.ufanet.meetingsbot.handler.type.ChatType;
import lombok.Getter;
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
    private long x = 0;

    @Autowired
    public TelegramBot(BotConfig botConfig, List<ChatHandler> chatHandlers, SetMyCommands setMyCommands) {
        this.botConfig = botConfig;
        safeExecute(setMyCommands);
        chatHandlers.forEach(handler -> this.chatHandlers.put(handler.getMessageType(), handler));
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null) return;
        UpdateDto updateDto = UpdateService.parseUpdate(update);
        ChatType chat = ChatType.typeOf(updateDto.chatType());
        x = System.currentTimeMillis();
        log.info("received message from {}", updateDto.chatId());
        //TODO maybe replace with iteration on hashmap
        switch (chat) {
            case PRIVATE -> chatHandlers.get(ChatType.PRIVATE).chatUpdate(update);
            case GROUP, SUPERGROUP -> chatHandlers.get(ChatType.GROUP).chatUpdate(update);
        }
    }

    public Serializable safeExecute(BotApiMethod<?> message) {
        try {
            log.info("message process time {}", System.currentTimeMillis() - x);
            return execute(message);
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