package com.ufanet.meetingsbot.message.keyboard;

import com.ufanet.meetingsbot.utils.Emojis;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public abstract class KeyboardMaker {
    public InlineKeyboardButton defaultInlineButton(String text, String callback) {
        return InlineKeyboardButton.builder().text(text)
                .callbackData(callback).build();
    }

    public InlineKeyboardButton getReadyInlineButton(String callback) {
        return defaultInlineButton(Emojis.GREEN_SELECTED.getEmojiSpace() + "Готово", callback);
    }

    public InlineKeyboardButton getNextInlineButton(String callback) {
        return defaultInlineButton("Далее " + Emojis.RIGHT.getEmoji(), callback);
    }

    public InlineKeyboardButton getCancelInlineButton(String callback) {
        return defaultInlineButton(Emojis.CANCEL_CIRCLE.getEmojiSpace() + "Отменить", callback);
    }

    public InlineKeyboardButton getPreviousInlineButton(String callback) {
        return defaultInlineButton(Emojis.LEFT.getEmojiSpace() + "Назад", callback);
    }

    public InlineKeyboardMarkup buildInlineMarkup(List<List<InlineKeyboardButton>> keyboard) {
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }
}
