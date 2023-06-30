package com.ufanet.meetingsbot.exceptions;

public class AccountNotFoundException extends CustomTelegramApiException {

    public AccountNotFoundException(Long chatId) {
        super(chatId, "Пользователь не найден!");
    }
    public AccountNotFoundException(){
        super("Пользователь не найден!");
    }
}
