package com.ufanet.meetingsbot.botapi;

import com.ufanet.meetingsbot.botapi.handlers.message.MessageHandler;
import com.ufanet.meetingsbot.service.MessageService;
import com.ufanet.meetingsbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramFacade {
    private final List<MessageHandler> messageHandlers;
    private final MessageService messageService;
    private final UserService userService;

//    public BotApiMethod<?> updateProcessor(Update update) {
//        Message message = update.getMessage();
//        if (update.hasCallbackQuery()) {
//            String type = message.getChat().getType();
//            return messageHandlers.stream()
//                    .filter((handler) -> handler.getMessageType().equals(type))
//                    .map(handler -> handler.handleUpdate(update)).findFirst()
//                    .orElse(messageService.getWarningMessage(message.getChatId()));
//        }
//        return null;
//    }
}
