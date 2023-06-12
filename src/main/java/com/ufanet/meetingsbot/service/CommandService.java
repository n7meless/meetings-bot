package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.handler.message.CommandReplyMessageHandler;
import com.ufanet.meetingsbot.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommandService {
    private final CommandReplyMessageHandler commandHandler;
    private final AccountService accountService;
    public void handle(long userId, Message message) {
        Long chatId = message.getChat().getId();
        String text = message.getText();
        User user = message.getFrom();
        switch (text) {
            case "/start" -> {
                Optional<Account> account = accountService.getById(chatId);
                if (account.isEmpty()){
                    accountService.saveTgUser(user);
                }
                commandHandler.sendStartMessage(userId);
            }
            case "/help" -> commandHandler.sendHelpMessage(userId);
            case "/about" -> commandHandler.sendAboutMessage(userId);
        }
    }
}
