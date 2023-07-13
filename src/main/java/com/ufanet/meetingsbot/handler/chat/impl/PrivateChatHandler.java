package com.ufanet.meetingsbot.handler.chat.impl;

import com.ufanet.meetingsbot.constants.BotCommands;
import com.ufanet.meetingsbot.constants.state.ProfileState;
import com.ufanet.meetingsbot.constants.type.ChatType;
import com.ufanet.meetingsbot.constants.type.EventType;
import com.ufanet.meetingsbot.handler.chat.ChatHandler;
import com.ufanet.meetingsbot.handler.event.EventHandler;
import com.ufanet.meetingsbot.message.CommandReplyMessage;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.BotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ufanet.meetingsbot.constants.type.EventType.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateChatHandler implements ChatHandler {
    private final Map<EventType, EventHandler> eventHandlers = new HashMap<>();
    private final AccountService accountService;
    private final CommandReplyMessage commandMessage;
    private final BotService botService;

    @Override
    public void handleChatUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            long userId = message.getChatId();

            log.info("received message from user {}", userId);
            handleMessage(userId, update);
        } else if (update.hasInlineQuery()) {
            long userId = update.getInlineQuery().getFrom().getId();

            log.info("received inline query from user {}", userId);
            botService.setState(userId, ProfileState.PROFILE_TIMEZONE_SELECT.name());
            handleBotState(userId, update);
        } else if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            Message message = query.getMessage();
            long userId = message.getChatId();
            String data = query.getData();
            if (data.isBlank()) {
                commandMessage.executeNullCallback(query.getId());
            } else {
                log.info("received callback query from user {}", userId);
                if (EventType.startWithState(data)) {
                    botService.setState(userId, data);
                }
                handleBotState(userId, update);
            }
        }
    }

    protected void handleMessage(long userId, Update update) {
        Message message = update.getMessage();
        String messageText = message.getText();

        if (messageText.startsWith("/")) {
            handleCommand(update);
        } else {
            botService.setLastMessageFromBot(userId, false);
            EventType pressedButton = fromValue(messageText);
            if (pressedButton != null) {
                botService.setState(userId, pressedButton.name());
            }
            handleBotState(userId, update);

        }
    }

    protected void handleCommand(Update update) {
        Message message = update.getMessage();
        long userId = message.getChat().getId();
        String messageText = message.getText();
        User user = message.getFrom();
        log.info("handle command in private chat from user {}", userId);

        if (messageText.startsWith(BotCommands.START.getCommand())) {

            accountService.getByUserId(userId)
                    .ifPresentOrElse((account) -> accountService.updateFromTgUser(account, user),
                            () -> accountService.createAccount(user));
            commandMessage.sendStartMessage(userId);
        } else if (messageText.startsWith(BotCommands.HELP.getCommand())) {
            commandMessage.sendHelpMessage(userId);
        } else if (messageText.startsWith(BotCommands.ABOUT.getCommand())) {
            commandMessage.sendAboutMessage(userId);
        } else if (messageText.startsWith(BotCommands.SETTIMEZONE.getCommand())) {
            eventHandlers.get(PROFILE).handleUpdate(update);
        }
    }

    protected void handleBotState(long userId, Update update) {
        String state = botService.getState(userId);
        if (state.startsWith(CREATE.name())) {
            eventHandlers.get(CREATE).handleUpdate(update);
        } else if (state.startsWith(UPCOMING.name())) {
            eventHandlers.get(UPCOMING).handleUpdate(update);
        } else if (state.startsWith(PREVIOUS.name())) {
            eventHandlers.get(PREVIOUS).handleUpdate(update);
        } else if (state.startsWith(PROFILE.name())) {
            eventHandlers.get(PROFILE).handleUpdate(update);
        } else if (state.startsWith(EDIT.name())) {
            eventHandlers.get(EDIT).handleUpdate(update);
        }
    }

    @Autowired
    private void setEventHandlers(List<EventHandler> eventHandlers) {
        eventHandlers.forEach(handler ->
                this.eventHandlers.put(handler.getEventType(), handler));
    }

    @Override
    public ChatType getChatType() {
        return ChatType.PRIVATE;
    }
}
