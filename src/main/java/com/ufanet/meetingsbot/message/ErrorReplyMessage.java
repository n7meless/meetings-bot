package com.ufanet.meetingsbot.message;

import com.ufanet.meetingsbot.exceptions.AccountNotFoundException;
import com.ufanet.meetingsbot.exceptions.NullCallbackException;
import com.ufanet.meetingsbot.utils.Emojis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@RestControllerAdvice
public class ErrorReplyMessage extends ReplyMessage {

    @ExceptionHandler
    public void userNotFoundExceptionHandler(AccountNotFoundException e) {
        executeErrorMessage(e.getChatId(), e.getMessage());
    }

    @ExceptionHandler
    public void anyExceptionHandler(Exception e) {
        e.printStackTrace();
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

    protected void executeErrorMessage(long userId, String message) {
        SendMessage sendMessage = messageUtils.generateSendMessage(userId,
                Emojis.WARNING.getEmojiSpace() + message);
        executeSendMessage(sendMessage);
    }
}
