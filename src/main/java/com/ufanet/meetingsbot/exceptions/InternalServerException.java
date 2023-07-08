package com.ufanet.meetingsbot.exceptions;

public class InternalServerException extends CustomTelegramApiException {

    public InternalServerException(Long chatId, String messageProperty) {
        super(chatId, messageProperty);
    }
}
