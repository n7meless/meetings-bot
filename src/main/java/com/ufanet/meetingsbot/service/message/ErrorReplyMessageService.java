package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.exceptions.UserNotFoundException;
import com.ufanet.meetingsbot.utils.Emojis;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@RestControllerAdvice
public class ErrorReplyMessageService extends ReplyMessageService {

    @ExceptionHandler
    public void userNotFoundExceptionHandler(UserNotFoundException e) {
        executeErrorMessage(e.getChatId(), e.getMessage());
    }
    @ExceptionHandler
    public void anyExceptionHandler(Exception e){
        e.printStackTrace();
    }

    protected void executeErrorMessage(long userId, String message) {
        SendMessage sendMessage = messageUtils.generateSendMessage(userId,
                Emojis.WARNING.getEmojiSpace() + message);
        telegramBot.safeExecute(sendMessage);
    }
}
