package com.ufanet.meetingsbot.service.message;

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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Slf4j
@Service
public abstract class ReplyMessageService {
    protected TelegramBot telegramBot;
    protected BotService botService;
    protected MessageUtils messageUtils;
    protected LocaleMessageService localeMessageService;

    void executeSendMessage(SendMessage message) {
        long chatId = Long.parseLong(message.getChatId());
        BotState botState = botService.getByUserId(chatId);

        disableInlineMessage(chatId, botState.getMessageId());
        log.info("send message to {}", chatId);
        Message response = (Message) telegramBot.safeExecute(message);

        if (response != null) {
            botState.setMessageId(response.getMessageId());
        }
        botState.setMessageType(MessageType.SEND_MESSAGE);
        botService.save(botState);
    }

    void executeEditMessage(EditMessageText message) {
        long chatId = Long.parseLong(message.getChatId());
        int messageId = botService.getByUserId(chatId).getMessageId();
        message.setMessageId(messageId);
        try {
            telegramBot.execute(message);
        } catch (TelegramApiRequestException e) {
            log.error("message {} is not modified", message.getMessageId());
            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(chatId, message.getText(), message.getReplyMarkup());

            executeSendMessage(sendMessage);
        } catch (TelegramApiException e) {
            log.error("an occurred error when sending edit message with id {}", message.getMessageId());
        }
    }
    protected void disableInlineMessage(Long userId, Integer messageId) {
        if (messageId == null) return;
        try {
            EditMessageReplyMarkup disableMarkup = EditMessageReplyMarkup.builder()
                    .chatId(userId).messageId(messageId)
                    .replyMarkup(null).build();
            log.info("disable inline markup with message id {}", messageId);
            telegramBot.execute(disableMarkup);
        } catch (TelegramApiException e) {
            log.error("can not disable inline markup with message id {}", messageId);
        }
    }

    @Autowired
    private void setDependencies(@Lazy TelegramBot telegramBot, BotService botService,
                                 MessageUtils messageUtils, LocaleMessageService localeMessageService) {
        this.botService = botService;
        this.telegramBot = telegramBot;
        this.messageUtils = messageUtils;
        this.localeMessageService = localeMessageService;
    }
}
