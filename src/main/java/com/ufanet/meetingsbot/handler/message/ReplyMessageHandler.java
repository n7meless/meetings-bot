package com.ufanet.meetingsbot.handler.message;

import com.ufanet.meetingsbot.cache.impl.BotMessageCache;
import com.ufanet.meetingsbot.service.TelegramBot;
import com.ufanet.meetingsbot.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

@Service
public abstract class ReplyMessageHandler {
    protected TelegramBot telegramBot;
    protected final BotMessageCache messageCache;
    protected final MessageUtils messageUtils;

    public ReplyMessageHandler(BotMessageCache messageCache,
                               MessageUtils messageUtils) {
        this.messageCache = messageCache;
        this.messageUtils = messageUtils;
    }

    @Autowired
    public void setTelegramBot(@Lazy TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    //template method
    public final void replyMessage(long userId, String message) {
        disableInlineLastMessage(userId);
        handle(userId, message);
    }

    protected abstract void handle(long userId, String message);

    protected void disableInlineLastMessage(long userId) {
        Integer messageId = messageCache.get(userId);
        if (messageId == 0) return;
        EditMessageText disableMarkup =
                messageUtils.disableInlineMarkup(userId, messageId);
        telegramBot.safeExecute(disableMarkup);
    }


}
