package com.ufanet.meetingsbot.exceptions;

public class AccountTimeNotFoundException extends CustomTelegramApiException {

    public AccountTimeNotFoundException(Long chatId) {
        super(chatId, "error.accounttime.notexists");
    }
}
