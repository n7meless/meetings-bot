package com.ufanet.meetingsbot.message;

import com.ufanet.meetingsbot.constants.type.MessageType;
import com.ufanet.meetingsbot.entity.BotState;
import com.ufanet.meetingsbot.service.BotService;
import com.ufanet.meetingsbot.service.LocaleMessageService;
import com.ufanet.meetingsbot.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public abstract class ReplyMessage {
    private BotService botService;
    private ExecutorService executorService;
    protected DefaultAbsSender absSender;
    protected MessageUtils messageUtils;
    protected LocaleMessageService localeMessageService;


    protected void executeSendMessage(SendMessage message) {
        executorService.execute(() -> {
            long chatId = Long.parseLong(message.getChatId());
            BotState botState = botService.getByUserId(chatId);

            Integer messageId = botState.getMessageId();
            if (messageId != null) {
                disableInlineMessage(chatId, messageId);
            }

            log.info("send message to {}", chatId);
            try {
                Message response = absSender.execute(message);
                if (response != null) {
                    botState.setMessageId(response.getMessageId());
                }
            } catch (TelegramApiException e) {
                log.error("an occurred error when sending message to {}", chatId);
            }

            botState.setMsgFromBot(true);
            botState.setMessageType(MessageType.SEND_MESSAGE);
            botService.saveCache(chatId, botState);
        });
    }

    protected void safeExecute(BotApiMethod<?> method) {
        try {
            absSender.execute(method);
        } catch (TelegramApiException e) {
            log.error("an occurred error when execute method: {}", e.getMessage());
        }
    }

    protected void executeEditOrSendMessage(EditMessageText message) {
        long chatId = Long.parseLong(message.getChatId());
        BotState botState = botService.getByUserId(chatId);
        if (botState == null || !botState.isMsgFromBot()) {
            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(chatId, message.getText(),
                            message.getReplyMarkup());

            executeSendMessage(sendMessage);
        } else {
            int messageId = botState.getMessageId();
            message.setMessageId(messageId);
            executeEditMessage(message);
            botState.setMessageType(MessageType.EDIT_MESSAGE);
            botService.saveCache(chatId, botState);
        }
    }

    protected void executeEditMessage(EditMessageText message) {
        long chatId = Long.parseLong(message.getChatId());
        int messageId = message.getMessageId();
        try {
            log.info("send edit message to {} with messageId {}", chatId, messageId);
            absSender.execute(message);
        } catch (TelegramApiRequestException e) {
            log.warn(e.getMessage());
            log.warn("message {} in chat {} has not been modified", messageId, chatId);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
            log.error("an occurred error when sending message {} in chat {}", messageId, chatId);
        }
    }

    public void executeNullCallback(String callbackId) {
        AnswerCallbackQuery nullCallback = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackId).build();
        try {
            log.info("execute null callback {}", callbackId);
            absSender.execute(nullCallback);
        } catch (TelegramApiException ex) {
            log.error("an occurred error when answer callback {}", callbackId);
        }
    }

    protected void disableInlineMessage(Long userId, Integer messageId) {
        if (messageId == null) return;
        try {
            EditMessageReplyMarkup disableMarkup = EditMessageReplyMarkup.builder()
                    .chatId(userId).messageId(messageId)
                    .replyMarkup(null).build();
            log.info("disable inline markup to user {} with message id {}", userId, messageId);
            absSender.execute(disableMarkup);
        } catch (TelegramApiException e) {
            log.warn("can not disable inline markup to user {} with message id {}", userId, messageId);
        }
    }

    @Autowired
    private void setDependencies(@Lazy DefaultAbsSender absSender, BotService botService,
                                 MessageUtils messageUtils, LocaleMessageService localeMessageService,
                                 ExecutorService executorService) {
        this.executorService = executorService;
        this.botService = botService;
        this.absSender = absSender;
        this.messageUtils = messageUtils;
        this.localeMessageService = localeMessageService;
    }
}
