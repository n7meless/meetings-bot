package com.ufanet.meetingsbot.handler.chat;

import com.ufanet.meetingsbot.constants.BotCommands;
import com.ufanet.meetingsbot.constants.ReplyKeyboardButton;
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

import static com.ufanet.meetingsbot.constants.ReplyKeyboardButton.fromValue;

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

        ReplyKeyboardButton button = fromValue(content);

        if (button != null) {
            handleReplyButton(userId, button);
            handleCallback(userId, update);
        } else if (BotCommands.typeOf(content)) {
            handleCommand(userId, message);
        } else if (content.startsWith("UPCOMING")) {
            accountService.setState(userId, AccountState.UPCOMING);
            queryHandlers.get(AccountState.UPCOMING).handleUpdate(update);
        } else {
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
            case CREATE -> queryHandlers.get(AccountState.CREATE).handleUpdate(update);
            case EDIT -> queryHandlers.get(AccountState.EDIT).handleUpdate(update);
            case PROFILE -> queryHandlers.get(AccountState.PROFILE).handleUpdate(update);
            case UPCOMING -> queryHandlers.get(AccountState.UPCOMING).handleUpdate(update);
        }
    }

    void handleReplyButton(long userId, ReplyKeyboardButton button) {
        switch (button) {
            case CREATE_MEETING -> accountService.setState(userId, AccountState.CREATE);
            case EDIT_MEETING -> accountService.setState(userId, AccountState.EDIT);
            case MY_PROFILE -> accountService.setState(userId, AccountState.PROFILE);
            case UPCOMING_MEETINGS -> accountService.setState(userId, AccountState.UPCOMING);
        }
    }

    @Autowired
    void setQueryHandlers(List<KeyboardHandler> keyboardHandlers) {
        keyboardHandlers.forEach(handler ->
                this.queryHandlers.put(handler.getUserStateHandler(), handler));
    }

    @Override
    public ChatType getMessageType() {
        return ChatType.PRIVATE;
    }
}
