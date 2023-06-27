package com.ufanet.meetingsbot.handler.event;

import com.ufanet.meetingsbot.constants.state.AccountState;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface EventHandler {

    void handleUpdate(Update update);

    AccountState getAccountStateHandler();
}
