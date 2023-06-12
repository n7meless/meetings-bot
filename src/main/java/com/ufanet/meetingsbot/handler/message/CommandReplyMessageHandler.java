package com.ufanet.meetingsbot.handler.message;

import com.ufanet.meetingsbot.cache.impl.BotMessageCache;
import com.ufanet.meetingsbot.keyboard.ReplyKeyboardMaker;
import com.ufanet.meetingsbot.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class CommandReplyMessageHandler extends ReplyMessageHandler{
    private final ReplyKeyboardMaker replyKeyboardMaker;

    @Autowired
    public CommandReplyMessageHandler(BotMessageCache messageCache,
                                      MessageUtils messageUtils, ReplyKeyboardMaker replyKeyboardMaker) {
        super(messageCache, messageUtils);
        this.replyKeyboardMaker = replyKeyboardMaker;
    }

    public void sendAboutMessage(long userId){
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


    @Override
    protected void handle(long userId, String message) {

    }
}
