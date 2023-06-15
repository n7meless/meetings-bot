package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.state.AccountState;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface KeyboardHandler {

    void handleUpdate(Update update);

    AccountState getUserStateHandler();

}
