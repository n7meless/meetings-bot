package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.type.MessageType;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public abstract class ReplyMessageService {
    protected TelegramBot telegramBot;
    protected BotService botService;
    protected MessageUtils messageUtils;
    protected LocaleMessageService localeMessageService;
    protected ExecutorService executorService;

    void executeSendMessage(SendMessage message) {
        executorService.execute(() -> {
            long chatId = Long.parseLong(message.getChatId());
            BotState botState = botService.getByUserId(chatId);

            Integer messageId = botState.getMessageId();
            if (messageId != null) {
                disableInlineMessage(chatId, messageId);
            }

            log.info("send message to {}", chatId);
            Message response = (Message) telegramBot.safeExecute(message);

            if (response != null) {
                botState.setMessageId(response.getMessageId());
            }
            botState.setMsgFromUser(false);
            botState.setMessageType(MessageType.SEND_MESSAGE);
            botService.saveCache(chatId, botState);
        });
    }

    void executeMessage(EditMessageText message) {
        long chatId = Long.parseLong(message.getChatId());
        BotState botState = botService.getByUserId(chatId);
        boolean fromUser = botState.isMsgFromUser();
        if (fromUser) {
            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(chatId, message.getText(),
                            message.getReplyMarkup());

            executeSendMessage(sendMessage);
        } else {
            executeEditMessage(message);
        }
    }

    protected void executeEditMessage(EditMessageText message) {
        long chatId = Long.parseLong(message.getChatId());
        BotState botState = botService.getByUserId(chatId);
        int messageId = botState.getMessageId();
        message.setMessageId(messageId);
        try {
            log.info("send edit message to {} with messageId {}", chatId, messageId);
            telegramBot.execute(message);
        } catch (TelegramApiRequestException e) {
            log.warn(e.getMessage());
            log.warn("message {} in chat {} has not been modified", messageId, chatId);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
            log.error("an occurred error when sending message {} in chat {}", messageId, chatId);
        }
        botState.setMessageType(MessageType.EDIT_MESSAGE);
        botService.saveCache(chatId, botState);
    }

    protected void disableInlineMessage(Long userId, Integer messageId) {
        if (messageId == null) return;
        try {
            EditMessageReplyMarkup disableMarkup = EditMessageReplyMarkup.builder()
                    .chatId(userId).messageId(messageId)
                    .replyMarkup(null).build();
            log.info("disable inline markup to user {} with message id {}", userId, messageId);
            telegramBot.execute(disableMarkup);
        } catch (TelegramApiException e) {
            log.warn("can not disable inline markup to user {} with message id {}", userId, messageId);
        }
    }

    @Autowired
    private void setDependencies(@Lazy TelegramBot telegramBot, BotService botService,
                                 MessageUtils messageUtils, LocaleMessageService localeMessageService,
                                 ExecutorService executorService) {
        this.executorService = executorService;
        this.botService = botService;
        this.telegramBot = telegramBot;
        this.messageUtils = messageUtils;
        this.localeMessageService = localeMessageService;
    }
}
