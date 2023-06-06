package com.ufanet.meetingsbot.botapi;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Getter
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${telegram.username}")
    String botUsername;
    @Value("${telegram.botToken}")
    String botToken;

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage() != null){
            Long chatId = update.getMessage().getChatId();
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "hello!");
            execute(sendMessage);
        }
    }
}