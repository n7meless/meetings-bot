package com.ufanet.meetingsbot.handler.message;

import com.ufanet.meetingsbot.service.TelegramBot;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public abstract class MessageHandler {
    protected final TelegramBot telegramBot;

    protected MessageHandler(@Lazy TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public abstract void handleMessage(Update update);

}
