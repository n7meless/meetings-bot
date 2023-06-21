package com.ufanet.meetingsbot.dto;

import org.telegram.telegrambots.meta.api.objects.User;

public record UpdateDto(long chatId, String content, String chatType) {}
