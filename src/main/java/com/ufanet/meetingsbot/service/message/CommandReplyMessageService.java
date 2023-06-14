package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.keyboard.ReplyKeyboardMaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@RequiredArgsConstructor
public class CommandReplyMessageService extends ReplyMessageService {
    private final ReplyKeyboardMaker replyKeyboardMaker;

    public void sendAboutMessage(long userId) {
        SendMessage aboutMessage = messageUtils.generateSendMessage(userId,
                "Здесь написано обо мне");
        telegramBot.safeExecute(aboutMessage);
    }

    public void sendStartMessage(long userId) {
        SendMessage startMessage = messageUtils.generateSendMessage(userId,
                "Выберите опцию с помощью клавиатуры снизу",
                replyKeyboardMaker.getMainMenuKeyboard());
        telegramBot.safeExecute(startMessage);

    }

    public void sendHelpMessage(long userId) {
        SendMessage helpMessage = messageUtils.generateSendMessage(userId,
                "Здесь я вам помогу");
        telegramBot.safeExecute(helpMessage);
    }

}
