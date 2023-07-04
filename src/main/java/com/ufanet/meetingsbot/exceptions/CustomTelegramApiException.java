package com.ufanet.meetingsbot.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class CustomTelegramApiException extends ResponseStatusException {
    private Long chatId;
    private String messageProperty;

    public CustomTelegramApiException(Long chatId, String messageProperty) {
        super(HttpStatus.OK);
        this.chatId = chatId;
        this.messageProperty = messageProperty;
    }

    public CustomTelegramApiException(String messageProperty) {
        super(HttpStatus.OK);
        this.messageProperty = messageProperty;
    }

    public CustomTelegramApiException(Long chatId) {
        super(HttpStatus.OK);
        this.chatId = chatId;
    }
}
