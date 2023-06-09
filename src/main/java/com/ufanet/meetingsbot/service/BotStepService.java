package com.ufanet.meetingsbot.service;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

public interface BotStepService {
    BotApiMethod<?> next(long userId, String data);
    boolean cancel(long userId, String data);
    BotApiMethod<?> previous(long userId);
}
