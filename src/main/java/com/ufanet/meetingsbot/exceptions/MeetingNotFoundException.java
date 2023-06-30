package com.ufanet.meetingsbot.exceptions;

public class MeetingNotFoundException extends CustomTelegramApiException{
    public MeetingNotFoundException(Long chatId) {
        super(chatId);
    }
}
