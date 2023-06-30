package com.ufanet.meetingsbot.exceptions;

public class GroupNotFoundException extends CustomTelegramApiException{
    public GroupNotFoundException(Long chatId) {
        super(chatId);
    }
}
