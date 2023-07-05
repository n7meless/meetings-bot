package com.ufanet.meetingsbot.handler.chat.impl;

import com.ufanet.meetingsbot.constants.BotCommands;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.ProfileState;
import com.ufanet.meetingsbot.constants.type.ChatType;
import com.ufanet.meetingsbot.exceptions.NullCallbackException;
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

import static com.ufanet.meetingsbot.constants.state.AccountState.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateChatHandler implements ChatHandler {

    private final Map<AccountState, EventHandler> queryHandlers = new HashMap<>();
    private final AccountService accountService;
    private final CommandReplyMessage commandHandler;
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

            if (data.isBlank()) throw new NullCallbackException(query.getId());

            log.info("received callback query from user {}", userId);
            if (AccountState.startWithState(data)) {
                botService.setState(userId, data);
            }
            handleBotState(userId, update);
        }
    }

    protected void handleMessage(long userId, Update update) {
        Message message = update.getMessage();
        String messageText = message.getText();

        if (messageText.startsWith("/")) {
            handleCommand(update);
        } else {
            botService.setLastMsgFromUser(userId, true);
            AccountState pressedButton = fromValue(messageText);
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
        if (messageText.startsWith(BotCommands.START.getCommand())) {

            accountService.getByUserId(userId)
                    .ifPresentOrElse((account) -> accountService.updateTgUser(account, user),
                            () -> accountService.saveTgUser(user));
            commandHandler.sendStartMessage(userId);
        } else if (messageText.startsWith(BotCommands.HELP.getCommand())) {
            commandHandler.sendHelpMessage(userId);
        } else if (messageText.startsWith(BotCommands.ABOUT.getCommand())) {
            commandHandler.sendAboutMessage(userId);
        } else if (messageText.startsWith(BotCommands.SETTIMEZONE.getCommand())) {
            queryHandlers.get(PROFILE).handleUpdate(update);
        }
    }

    protected void handleBotState(long userId, Update update) {
        String state = botService.getState(userId);
        if (state.startsWith(CREATE.name())) {
            queryHandlers.get(CREATE).handleUpdate(update);
        } else if (state.startsWith(UPCOMING.name())) {
            queryHandlers.get(UPCOMING).handleUpdate(update);
        } else if (state.startsWith(PREVIOUS.name())) {
            queryHandlers.get(PREVIOUS).handleUpdate(update);
        } else if (state.startsWith(PROFILE.name())) {
            queryHandlers.get(PROFILE).handleUpdate(update);
        } else if (state.startsWith(EDIT.name())) {
            queryHandlers.get(EDIT).handleUpdate(update);
        }
    }

    @Autowired
    private void setQueryHandlers(List<EventHandler> eventHandlers) {
        eventHandlers.forEach(handler ->
                this.queryHandlers.put(handler.getAccountStateHandler(), handler));
    }

    @Override
    public ChatType getChatType() {
        return ChatType.PRIVATE;
    }
}
