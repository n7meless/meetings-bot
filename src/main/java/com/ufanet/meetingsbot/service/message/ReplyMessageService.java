package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.cache.impl.BotMessageCache;
import com.ufanet.meetingsbot.service.TelegramBot;
import com.ufanet.meetingsbot.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;

@Service
@Slf4j
public abstract class ReplyMessageService {
    protected TelegramBot telegramBot;
    protected BotMessageCache messageCache;
    protected MessageUtils messageUtils;


    protected void disableInlineLastMessage(long userId) {
        Integer messageId = messageCache.get(userId);
        if (messageId == 0) return;
        try {
            EditMessageReplyMarkup disableMarkup = EditMessageReplyMarkup.builder()
                    .chatId(userId).messageId(messageId)
                    .replyMarkup(null).build();
            log.info("disable inline markup with message id {}", messageId );
            telegramBot.safeExecute(disableMarkup);
        } catch (Exception e){
            log.error("error when hide reply");
        }
    }

    @Autowired
    public void setDefaultDependencies(@Lazy TelegramBot telegramBot, BotMessageCache messageCache,
                                       MessageUtils messageUtils) {
        this.telegramBot = telegramBot;
        this.messageCache = messageCache;
        this.messageUtils = messageUtils;
    }

}
