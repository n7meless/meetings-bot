package com.ufanet.meetingsbot.handler.chat;

import com.ufanet.meetingsbot.cache.impl.AccountStateCache;
import com.ufanet.meetingsbot.constants.DefaultCommand;
import com.ufanet.meetingsbot.constants.ReplyKeyboardButton;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.handler.keyboard.KeyboardHandler;
import com.ufanet.meetingsbot.handler.message.CommandReplyMessageHandler;
import com.ufanet.meetingsbot.handler.type.ChatType;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.CommandService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.state.AccountState;
import com.ufanet.meetingsbot.state.MeetingState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ufanet.meetingsbot.constants.ReplyKeyboardButton.fromValue;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateChatHandler implements ChatHandler {
    private final Map<AccountState, KeyboardHandler> queryHandlers = new HashMap<>();
    private final MeetingService meetingService;
    private final AccountStateCache accountStateCache;
    private final CommandService commandService;

    @Override
    public BotApiMethod<?> handleUpdate(Update update) {
        Message message = update.getMessage();
        UpdateDto updateDto = UpdateService.parseUpdate(update);
        long userId = updateDto.chatId();
        String content = updateDto.content();

        log.info("handle update from private chat with user {}", userId);
        ReplyKeyboardButton button = fromValue(content);
        if (button != null) {
            handleReplyButton(userId, button);
            handleCallback(userId, update);
        } else if (DefaultCommand.isCommand(content)) {
            commandService.handle(userId, message);
        } else {
            handleCallback(userId, update);
        }
        return null;
    }

    void handleCallback(long userId, Update update) {
        AccountState state = accountStateCache.get(userId);
        switch (state) {
            case CREATE -> queryHandlers.get(AccountState.CREATE).handleUpdate(update);
            case UPDATE -> queryHandlers.get(AccountState.UPDATE).handleUpdate(update);
            case PROFILE -> queryHandlers.get(AccountState.PROFILE).handleUpdate(update);
            case UPCOMING -> queryHandlers.get(AccountState.UPCOMING).handleUpdate(update);
        }
    }

    void handleReplyButton(long userId, ReplyKeyboardButton button) {
        switch (button) {
            case CREATE_MEETING -> {
                accountStateCache.put(userId, AccountState.CREATE);
                meetingService.updateState(userId, MeetingState.GROUP_SELECTION);
            }
            case EDIT_MEETING -> accountStateCache.put(userId, AccountState.UPDATE);
            case MY_PROFILE -> accountStateCache.put(userId, AccountState.PROFILE);
            case UPCOMING_MEETINGS -> accountStateCache.put(userId, AccountState.UPCOMING);
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
