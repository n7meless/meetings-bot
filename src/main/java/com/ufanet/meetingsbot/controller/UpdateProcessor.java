package com.ufanet.meetingsbot.controller;

import com.ufanet.meetingsbot.keyboard.MeetingInlineKeyboardMaker;
import com.ufanet.meetingsbot.service.TelegramBot;
import com.ufanet.meetingsbot.utils.MessageUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateProcessor {
    private final TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final MeetingInlineKeyboardMaker meetingInlineKeyboardMaker;

    public UpdateProcessor(@Lazy TelegramBot telegramBot, MessageUtils messageUtils, MeetingInlineKeyboardMaker meetingInlineKeyboardMaker) {
        this.telegramBot = telegramBot;
        this.messageUtils = messageUtils;
        this.meetingInlineKeyboardMaker = meetingInlineKeyboardMaker;
    }
    public void processUpdate(Update update) {
        if (update == null) {
            return;
        }
    }

    private void setView(BotApiMethod<?> message) {
        telegramBot.safeExecute(message);
    }
}
