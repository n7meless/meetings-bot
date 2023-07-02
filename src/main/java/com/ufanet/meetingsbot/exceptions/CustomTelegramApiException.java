package com.ufanet.meetingsbot.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
public abstract class CustomTelegramApiException extends ResponseStatusException {
    private Long chatId;
    private String message;

    public CustomTelegramApiException(Long chatId, String message) {
        super(HttpStatus.OK);
        this.chatId = chatId;
        this.message = message;
    }

    public CustomTelegramApiException(String message) {
        super(HttpStatus.OK);
        this.message = message;
    }

    public CustomTelegramApiException(Long chatId) {
        super(HttpStatus.OK);
        this.chatId = chatId;
    }
}
