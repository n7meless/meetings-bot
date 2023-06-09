package com.ufanet.meetingsbot.botapi;

import com.ufanet.meetingsbot.botapi.handlers.message.MessageHandler;
import com.ufanet.meetingsbot.botapi.handlers.type.ChatType;
import com.ufanet.meetingsbot.config.BotConfig;
import com.ufanet.meetingsbot.controller.UpdateProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UpdateProcessor updateProcessor;
    private final Map<ChatType, MessageHandler> messageHandlers = new HashMap<>();

    @Autowired
    public TelegramBot(BotConfig botConfig, UpdateProcessor updateProcessor,
                       List<MessageHandler> messageHandlers) {
        this.botConfig = botConfig;
        this.updateProcessor = updateProcessor;
        messageHandlers.forEach(handler -> this.messageHandlers.put(handler.getMessageType(), handler));
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null) return;
        String type = update.getMessage().getChat().getType();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    public void sendAnswerMessage(BotApiMethod<?>... messages) {
        try {
            for (BotApiMethod<?> message : messages) {
                execute(message);
            }
        } catch (TelegramApiException e) {
            log.error("error");
        }
    }
}