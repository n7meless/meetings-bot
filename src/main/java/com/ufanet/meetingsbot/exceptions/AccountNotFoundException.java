package com.ufanet.meetingsbot.exceptions;

public class AccountNotFoundException extends CustomTelegramApiException {

    public AccountNotFoundException(Long chatId) {
        super(chatId, "error.account.notexists");
    }
}
