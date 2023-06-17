package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.cache.impl.BotMessageCache;
import com.ufanet.meetingsbot.service.LocaleMessageService;
import com.ufanet.meetingsbot.service.TelegramBot;
import com.ufanet.meetingsbot.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;

@Service
@Slf4j
public abstract class MessageService {
    protected TelegramBot telegramBot;
    protected BotMessageCache messageCache;
    protected MessageUtils messageUtils;
    protected LocaleMessageService localeMessageService;


    protected void disableInlineLastMessage(long userId) {
        Integer messageId = messageCache.get(userId);
        if (messageId == 0) return;
        EditMessageReplyMarkup disableMarkup = EditMessageReplyMarkup.builder()
                .chatId(userId).messageId(messageId)
                .replyMarkup(null).build();

        log.info("disable inline markup with message id {}", messageId);
        telegramBot.safeExecute(disableMarkup);
    }
    protected void deleteLastMessage(long userId){
        Integer messageId = messageCache.get(userId);
        if (messageId == 0) return;
        DeleteMessage deleteMessage = DeleteMessage.builder().chatId(userId).messageId(messageId).build();
        log.info("delete message id {}", messageId);
        telegramBot.safeExecute(deleteMessage);
    }

    @Autowired
    private void setDependencies(@Lazy TelegramBot telegramBot, BotMessageCache messageCache,
                                MessageUtils messageUtils, LocaleMessageService localeMessageService) {
        this.telegramBot = telegramBot;
        this.messageCache = messageCache;
        this.messageUtils = messageUtils;
        this.localeMessageService = localeMessageService;
    }
}
