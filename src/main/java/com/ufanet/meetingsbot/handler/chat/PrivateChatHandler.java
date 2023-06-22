package com.ufanet.meetingsbot.handler.chat;

import com.ufanet.meetingsbot.constants.BotCommands;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.handler.keyboard.KeyboardHandler;
import com.ufanet.meetingsbot.handler.type.ChatType;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.message.CommandReplyMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ufanet.meetingsbot.constants.state.AccountState.UPCOMING_MEETINGS;
import static com.ufanet.meetingsbot.constants.state.AccountState.fromValue;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateChatHandler implements ChatHandler {
    private final Map<AccountState, KeyboardHandler> queryHandlers = new HashMap<>();
    private final AccountService accountService;
    private final CommandReplyMessageService commandHandler;
    private final UpdateService updateService;

    @Override
    public void chatUpdate(Update update) {
        Message message = update.getMessage();
        UpdateDto updateDto = updateService.parseUpdate(update);
        long userId = updateDto.chatId();
        String content = updateDto.content();

        if (content.isBlank()) return;

        log.info("handle update from private chat with user {}", userId);

        AccountState button = fromValue(content);

        if (button != null) {
            handleReplyButton(userId, button);
            handleCallback(userId, update);
        } else if (BotCommands.typeOf(content)) {
            handleCommand(userId, message);
        } else {
            if (content.startsWith("UPCOMING")) {
                accountService.setState(userId, UPCOMING_MEETINGS);
            }
            handleCallback(userId, update);
        }
    }

    public void handleCommand(long userId, Message message) {
        Long chatId = message.getChat().getId();
        String text = message.getText();
        User user = message.getFrom();
        switch (text) {
            case "/start" -> {
                Optional<Account> optionalAccount = accountService.getByUserId(chatId);
                optionalAccount.ifPresentOrElse((account) -> accountService.updateTgUser(account, user),
                        () -> accountService.saveTgUser(user));
                commandHandler.sendStartMessage(userId);
            }
            case "/help" -> commandHandler.sendHelpMessage(userId);
            case "/about" -> commandHandler.sendAboutMessage(userId);
        }
    }

    void handleCallback(long userId, Update update) {
        AccountState state = accountService.getState(userId);
        switch (state) {
            case CREATE_MEETING -> queryHandlers.get(AccountState.CREATE_MEETING).handleUpdate(update);
            case EDIT_MEETING -> queryHandlers.get(AccountState.EDIT_MEETING).handleUpdate(update);
            case PROFILE_SETTINGS -> queryHandlers.get(AccountState.PROFILE_SETTINGS).handleUpdate(update);
            case UPCOMING_MEETINGS -> queryHandlers.get(AccountState.UPCOMING_MEETINGS).handleUpdate(update);
        }
    }

    void handleReplyButton(long userId, AccountState button) {
        switch (button) {
            case CREATE_MEETING -> accountService.setState(userId, AccountState.CREATE_MEETING);
            case EDIT_MEETING -> accountService.setState(userId, AccountState.EDIT_MEETING);
            case PROFILE_SETTINGS -> accountService.setState(userId, AccountState.PROFILE_SETTINGS);
            case UPCOMING_MEETINGS -> accountService.setState(userId, AccountState.UPCOMING_MEETINGS);
        }
    }

    @Autowired
    void setQueryHandlers(List<KeyboardHandler> keyboardHandlers) {
        keyboardHandlers.forEach(handler ->
                this.queryHandlers.put(handler.getAccountStateHandler(), handler));
    }

    @Override
    public ChatType getMessageType() {
        return ChatType.PRIVATE;
    }
}
