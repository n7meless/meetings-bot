package com.ufanet.meetingsbot.exceptions;

public class ValidationMeetingException extends CustomTelegramApiException{
    public ValidationMeetingException(Long chatId, String message) {
        super(chatId, message);
    }

    public ValidationMeetingException(String message) {
        super(message);
    }
}
