package com.ufanet.meetingsbot.controller;

import com.ufanet.meetingsbot.service.TelegramBot;
import com.ufanet.meetingsbot.botapi.TelegramFacade;
import com.ufanet.meetingsbot.keyboard.InlineKeyboardMaker;
import com.ufanet.meetingsbot.utils.MessageUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class UpdateProcessor {
    private final TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final InlineKeyboardMaker inlineKeyboardMaker;
    private final TelegramFacade chatFacade;

    public UpdateProcessor(@Lazy TelegramBot telegramBot, MessageUtils messageUtils, InlineKeyboardMaker inlineKeyboardMaker, TelegramFacade chatFacade) {
        this.telegramBot = telegramBot;
        this.messageUtils = messageUtils;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.chatFacade = chatFacade;
    }
    public void processUpdate(Update update) {
        if (update == null) {
            return;
        }
        Message message1 = update.getCallbackQuery().getMessage();
        Long chatId = message1.getChatId();
        int message = message1.getMessageId();
        SendMessage asda =
                    SendMessage.builder().text("asda").chatId(chatId)
                            .replyMarkup(inlineKeyboardMaker.getCalendarInlineMarkup()).build();
        setView(asda);
//        BotApiMethod<?> method = chatFacade.updateProcessor(update);
    }

    private void setView(BotApiMethod<?> message) {
        telegramBot.sendAnswerMessage(message);
    }
}
