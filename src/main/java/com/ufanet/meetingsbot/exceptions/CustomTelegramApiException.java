package com.ufanet.meetingsbot.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
public abstract class CustomTelegramApiException extends ResponseStatusException {

    private final Long chatId;
    private final String messageProperty;

    public CustomTelegramApiException(Long chatId, String messageProperty) {
        super(HttpStatus.OK);
        this.chatId = chatId;
        this.messageProperty = messageProperty;
    }
}
