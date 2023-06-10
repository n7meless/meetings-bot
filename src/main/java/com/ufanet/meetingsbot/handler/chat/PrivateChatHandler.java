package com.ufanet.meetingsbot.handler.chat;

import com.ufanet.meetingsbot.handler.update.UpdateHandler;
import com.ufanet.meetingsbot.handler.type.ChatType;
import com.ufanet.meetingsbot.handler.type.HandlerType;
import com.ufanet.meetingsbot.cache.impl.BotStateCache;
import com.ufanet.meetingsbot.service.MainMenuService;
import com.ufanet.meetingsbot.state.BotState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PrivateChatHandler implements ChatHandler {
    private final MainMenuService mainMenuService;
    private final BotStateCache botStateCache;
    private final Map<HandlerType, UpdateHandler> queryHandlers = new HashMap<>();

    public PrivateChatHandler(MainMenuService mainMenuService, BotStateCache botStateCache,
                              List<UpdateHandler> updateHandlers) {

        this.mainMenuService = mainMenuService;
        this.botStateCache = botStateCache;
        updateHandlers.stream().filter(handler->handler.getTypeHandler().isPrivateHandler())
                .forEach(handler->this.queryHandlers.put(handler.getTypeHandler(), handler));
    }

    @Override
    public BotApiMethod<?> handleUpdate(Update update) {
        Message message = update.getMessage();
        Long userId = message.getChatId();
        BotState state = botStateCache.get(userId);
        switch (state){
            case CREATE -> {queryHandlers.get(HandlerType.CREATE).handleUpdate(update);}
            case UPCOMING -> {queryHandlers.get(HandlerType.UPCOMING).handleUpdate(update);}
            case UPDATE -> {queryHandlers.get(HandlerType.EDIT).handleUpdate(update);}
            case PROFILE -> {queryHandlers.get(HandlerType.PROFILE).handleUpdate(update);}
        }
        String text = message.getText();
        if (mainMenuService.isMainCommand(text)){
            return mainMenuService.getMainMessage(userId);
        }
        return null;
    }

    @Override
    public ChatType getMessageType() {
        return ChatType.PRIVATE;
    }
}