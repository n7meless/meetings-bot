package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.config.TelegramFacade;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;

import java.io.Serializable;

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

    public Serializable safeExecute(BotApiMethod<?> method) {
        try {
            return execute(method);
        } catch (TelegramApiException e) {
            log.error("an occurred error when sending method");
            return null;
        }
    }


    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        telegramFacade.handleUpdate(update);
        return null;
    }

}