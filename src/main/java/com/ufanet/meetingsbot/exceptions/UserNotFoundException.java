package com.ufanet.meetingsbot.exceptions;

import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException extends CustomTelegramApiException {

    public UserNotFoundException(Long chatId) {
        super(chatId, "Пользователь не найден!");
    }
    public UserNotFoundException(){
        super("Пользователь не найден!");
    }
}
