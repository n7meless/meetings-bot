package com.ufanet.meetingsbot.message;

import com.ufanet.meetingsbot.message.keyboard.MainKeyboardMaker;
import com.ufanet.meetingsbot.utils.Emojis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandReplyMessage extends ReplyMessage {
    private final MainKeyboardMaker mainKeyboard;
    private final String[] rainbowEmotions = {Emojis.RED_CIRCLE.getEmoji(), Emojis.ORANGE_CIRCLE.getEmoji(),
            Emojis.YELLOW_CIRCLE.getEmoji(), Emojis.GREEN_CIRCLE.getEmoji(), Emojis.BLUE_CIRCLE.getEmoji(),
            Emojis.PURPLE_CIRCLE.getEmoji()};

    public void sendAboutMessage(long userId) {
        SendMessage aboutMessage = messageUtils.generateSendMessage(userId,
                "Здесь написано обо мне");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (String smile : rainbowEmotions) {
            InlineKeyboardButton build = InlineKeyboardButton.builder()
                    .text(smile).callbackData(" ").build();
            buttons.add(build);
        }
        markup.setKeyboard(List.of(buttons));
        aboutMessage.setReplyMarkup(markup);
        executeSendMessage(aboutMessage);
    }

    public void sendStartMessage(long userId) {
        SendMessage startMessage = messageUtils.generateSendMessage(userId,
                "Добро пожаловать!", mainKeyboard.getMainMenuKeyboard());
        executeSendMessage(startMessage);
    }

    public void sendHelpMessage(long userId) {
        SendMessage helpMessage = messageUtils.generateSendMessage(userId,
                "Здесь я вам помогу");
        executeSendMessage(helpMessage);
    }
}
