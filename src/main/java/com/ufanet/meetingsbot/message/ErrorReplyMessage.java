package com.ufanet.meetingsbot.message;

import com.ufanet.meetingsbot.constants.Emojis;
import com.ufanet.meetingsbot.exceptions.CustomTelegramApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@ControllerAdvice
public class ErrorReplyMessage extends ReplyMessage {
    @ExceptionHandler
    public void customTelegramApiExceptionHandler(CustomTelegramApiException e) {
        SendMessage sendMessage = messageUtils.generateSendMessage(e.getChatId(),
                Emojis.WARNING.getEmojiSpace() + localeMessageService.getMessage(e.getMessageProperty()));
        safeExecute(sendMessage);
    }
}
