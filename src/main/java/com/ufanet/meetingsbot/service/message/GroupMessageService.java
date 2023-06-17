package com.ufanet.meetingsbot.service.message;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class GroupMessageService extends MessageService {
    public void sendWelcomeChatMessage(long chatId){
        SendMessage sendPoll = new SendMessage();
        sendPoll.setChatId(chatId);
        sendPoll.setText("Я должен знать о вас!");
        sendPoll.setProtectContent(true);
        sendPoll.setReplyMarkup(InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(InlineKeyboardButton.builder()
                        .text("Я тут").callbackData("Я тут").build()))
                .build());

       telegramBot.safeExecute(sendPoll);
    }
}
