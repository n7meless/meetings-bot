package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.state.AccountState;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface KeyboardHandler {

    BotApiMethod<?> handleUpdate(Update update);

    AccountState getUserStateHandler();

}
