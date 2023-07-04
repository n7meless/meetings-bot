package com.ufanet.meetingsbot.message;

import com.ufanet.meetingsbot.exceptions.CustomTelegramApiException;
import com.ufanet.meetingsbot.exceptions.NullCallbackException;
import com.ufanet.meetingsbot.utils.Emojis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@ControllerAdvice
public class ErrorReplyMessage extends ReplyMessage {
    @ExceptionHandler
    public void customTelegramApiExceptionHandler(CustomTelegramApiException e) {
        SendMessage sendMessage = messageUtils.generateSendMessage(e.getChatId(),
                Emojis.WARNING.getEmojiSpace() + localeMessageService.getMessage(e.getMessageProperty()));
        safeExecute(sendMessage);
    }

    @ExceptionHandler
    public void nullCallbackExceptionHandler(NullCallbackException e) {
        AnswerCallbackQuery nullCallback = AnswerCallbackQuery.builder()
                .callbackQueryId(e.getCallbackId()).build();
        try {
            absSender.execute(nullCallback);
        } catch (TelegramApiException ex) {
            log.error("an occurred error when answer callback {}", e.getCallbackId());
        }
    }
}
