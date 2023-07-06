package com.ufanet.meetingsbot.handler.event;

import com.ufanet.meetingsbot.constants.type.EventType;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface EventHandler {

    void handleUpdate(Update update);

    EventType getEventType();
}
