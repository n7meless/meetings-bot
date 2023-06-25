package com.ufanet.meetingsbot.handler.chat;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.ProfileState;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.handler.keyboard.KeyboardHandler;
import com.ufanet.meetingsbot.handler.type.ChatType;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.MeetingService;
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

import static com.ufanet.meetingsbot.constants.state.AccountState.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateChatHandler implements ChatHandler {
    private final Map<AccountState, KeyboardHandler> queryHandlers = new HashMap<>();
    private final AccountService accountService;
    private final CommandReplyMessageService commandHandler;
    private final UpdateService updateService;
    private final BotService botService;
    private final MeetingService meetingService;

    @Override
    public void chatUpdate(Update update) {
        UpdateDto updateDto = updateService.parseUpdate(update);
        long userId = updateDto.chatId();
        String content = updateDto.content();

        log.info("handle update from private chat with user {}", userId);

        if (update.hasMessage()) {
            handleMessage(userId, update);
        } else if (update.hasInlineQuery()) {
            botService.setState(userId, ProfileState.PROFILE_TIMEZONE_SELECT);
            handleBotState(userId, update);
        } else if (update.hasCallbackQuery()) {
            if (AccountState.startWithState(content)) {
                botService.setState(userId, content);
            }
            handleBotState(userId, update);
        }
    }

    public void handleMessage(long userId, Update update) {
        Message message = update.getMessage();
        String messageText = message.getText();

        botService.setLastMsgFromUser(userId, true);
        //TODO вынести текст в пропертис
        AccountState pressedButton = fromValue(messageText);

        if (pressedButton != null) {
            botService.setState(userId, pressedButton);
//            meetingService.clearCache(userId);
        } else if (messageText.startsWith("/")) {
            handleCommand(userId, message);
        }
        handleBotState(userId, update);
    }

    public void handleCommand(long userId, Message message) {
        Long chatId = message.getChat().getId();
        String messageText = message.getText();
        User user = message.getFrom();
        switch (messageText) {
            case "/start" -> {
                accountService.getByUserId(chatId)
                        .ifPresentOrElse((account) -> accountService.updateTgUser(account, user),
                                () -> accountService.saveTgUser(user));
                commandHandler.sendLanguageSelectMessage(userId);
            }
            case "/help" -> commandHandler.sendHelpMessage(userId);
            case "/about" -> commandHandler.sendAboutMessage(userId);
        }
    }

    void handleBotState(long userId, Update update) {
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
    void setQueryHandlers(List<KeyboardHandler> keyboardHandlers) {
        keyboardHandlers.forEach(handler ->
                this.queryHandlers.put(handler.getAccountStateHandler(), handler));
    }

    @Override
    public ChatType getChatType() {
        return ChatType.PRIVATE;
    }
}
