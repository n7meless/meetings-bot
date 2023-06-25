package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.config.TelegramFacade;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.handler.chat.ChatHandler;
import com.ufanet.meetingsbot.handler.type.ChatType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramBot extends SpringWebhookBot {

    String botToken;
    String botUsername;
    String botPath;

    final TelegramFacade telegramFacade;
    public TelegramBot(TelegramFacade telegramFacade, SetWebhook setWebhook) {
        super(setWebhook);
        this.telegramFacade = telegramFacade;
    }

    public Serializable safeExecute(BotApiMethod<?> message) {
        try {
            return execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error("an occurred error when sending message");
            return null;
        }
    }


    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return telegramFacade.handleUpdate(update);
    }

}