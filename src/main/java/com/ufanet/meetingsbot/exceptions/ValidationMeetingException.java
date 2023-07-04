package com.ufanet.meetingsbot.exceptions;

public class ValidationMeetingException extends CustomTelegramApiException {
    public ValidationMeetingException(Long chatId, String messageProperty) {
        super(chatId, messageProperty);
    }

    public ValidationMeetingException(String messageProperty) {
        super(messageProperty);
    }
}
