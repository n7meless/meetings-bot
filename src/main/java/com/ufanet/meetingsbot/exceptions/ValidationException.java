package com.ufanet.meetingsbot.exceptions;

public class ValidationException extends CustomTelegramApiException {

    public ValidationException(Long chatId, String messageProperty) {
        super(chatId, messageProperty);
    }
}
