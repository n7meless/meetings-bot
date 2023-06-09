package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.constants.MainButtonNameEnum;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MessageService {


    public SendMessage getMainMessage(Update update) {
        Message message = update.getMessage();
        MainButtonNameEnum value =
                MainButtonNameEnum.valueOf(message.getText());
        switch (value) {
            case CREATE_MEETING -> {

            }
            case UPCOMING_MEETINGS -> {

            }
            case EDIT_MEETING -> {

            }
            case MY_PROFILE -> {

            }
        }
        return null;
    }

    public SendMessage getWarningMessage(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("i dont know what u want")
                .build();
    }
}
