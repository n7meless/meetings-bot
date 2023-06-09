package com.ufanet.meetingsbot.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
public class MessageUtils {
    public SendMessage generateSendMessageWithText(Long chatId, String text) {
        return SendMessage.builder()
                .text(text).chatId(chatId)
                .build();
    }
    public SendMessage generateSendMessageWithText(Long chatId, String text, InlineKeyboardMarkup markup) {
        return SendMessage.builder()
                .text(text).chatId(chatId)
                .replyMarkup(markup).build();
    }
    public EditMessageText generateEditMessageWithText(Long chatId, String text,
                                                       Integer messageId, InlineKeyboardMarkup markup){
        return EditMessageText.builder().chatId(chatId)
                .messageId(messageId).text(text)
                .replyMarkup(markup).build();
    }
    public EditMessageText disableInlineMarkupLastMessage(Long chatId, Integer messageId){
        return EditMessageText.builder().chatId(chatId).messageId(messageId)
                .replyMarkup(null).build();
    }
}
