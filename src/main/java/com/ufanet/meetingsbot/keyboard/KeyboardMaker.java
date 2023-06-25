package com.ufanet.meetingsbot.keyboard;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.utils.Emojis;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public abstract class KeyboardMaker {
    protected final InlineKeyboardButton stepNext = defaultInlineButton("Далее", ToggleButton.NEXT.name());
    protected final InlineKeyboardButton cancel = defaultInlineButton(Emojis.CANCEL_CIRCLE.getEmojiSpace() + "Отменить", ToggleButton.CANCEL.name());
    protected final InlineKeyboardButton ready = defaultInlineButton("Готово", ToggleButton.READY.name());

    public InlineKeyboardButton defaultInlineButton(String text, String callback) {
        return InlineKeyboardButton.builder().text(text)
                .callbackData(callback).build();
    }

    public InlineKeyboardButton getReadyInlineButton(String callback) {
        return defaultInlineButton("Готово", callback);
    }
    public InlineKeyboardMarkup buildInlineMarkup(List<List<InlineKeyboardButton>> keyboard) {
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }
}
