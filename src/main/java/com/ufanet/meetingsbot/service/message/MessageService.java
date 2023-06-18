package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.cache.impl.BotMessageCache;
import com.ufanet.meetingsbot.constants.MessageType;
import com.ufanet.meetingsbot.model.BotState;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.LocaleMessageService;
import com.ufanet.meetingsbot.service.TelegramBot;
import com.ufanet.meetingsbot.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@Slf4j
public abstract class MessageService {
    protected TelegramBot telegramBot;
    protected BotService botService;
    protected BotMessageCache botMessageCache;
    protected MessageUtils messageUtils;
    protected LocaleMessageService localeMessageService;

    void executeSendMessage(SendMessage message) {
        long chatId = Long.parseLong(message.getChatId());
        BotState botState = botService.getByUserId(chatId);
        Integer messageId = botState.getMessageId();

        disableInlineLastMessage(chatId, botState.getMessageId());

        Message response = (Message) telegramBot.safeExecute(message);

        botState.setMessageType(MessageType.SEND_MESSAGE);
        botState.setMessageId(response.getMessageId());
        botService.save(botState);
    }

    void executeEditMessage(EditMessageText message) {
        long chatId = Long.parseLong(message.getChatId());
        int messageId = botService.getLastMessageId(chatId);
        message.setMessageId(messageId);
        telegramBot.safeExecute(message);
    }

    protected void disableInlineLastMessage(Long userId, Integer messageId) {
        if (messageId == null) return;
        EditMessageReplyMarkup disableMarkup = EditMessageReplyMarkup.builder()
                .chatId(userId).messageId(messageId)
                .replyMarkup(null).build();

        log.info("disable inline markup with message id {}", messageId);
        telegramBot.safeExecute(disableMarkup);
    }

    protected void deleteLastMessage(long userId) {
//        Integer messageId = messageCache.get(userId);
//        if (messageId == 0) return;
//        DeleteMessage deleteMessage = DeleteMessage.builder().chatId(userId).messageId(messageId).build();
//        log.info("delete message id {}", messageId);
//        telegramBot.safeExecute(deleteMessage);
    }

    @Autowired
    private void setDependencies(@Lazy TelegramBot telegramBot, BotService botService,
                                 BotMessageCache messageCache,
                                 MessageUtils messageUtils, LocaleMessageService localeMessageService) {
        this.botService = botService;
        this.telegramBot = telegramBot;
        this.botMessageCache = messageCache;
        this.messageUtils = messageUtils;
        this.localeMessageService = localeMessageService;
    }
}
