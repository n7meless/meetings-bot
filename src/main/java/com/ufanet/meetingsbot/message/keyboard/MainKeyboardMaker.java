package com.ufanet.meetingsbot.message.keyboard;

import com.ufanet.meetingsbot.constants.Language;
import com.ufanet.meetingsbot.constants.state.ProfileState;
import com.ufanet.meetingsbot.utils.Emojis;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.ufanet.meetingsbot.constants.state.AccountState.*;

@Component
public class MainKeyboardMaker extends KeyboardMaker {

    public InlineKeyboardMarkup getLanguageInlineMarkup() {
        InlineKeyboardButton russian = defaultInlineButton(Emojis.RUSSIA.getEmojiSpace() +
                        Language.RUSSIAN.name(),
                ProfileState.PROFILE_LANGUAGE_SELECT.name() + Language.RUSSIAN);
        InlineKeyboardButton english = defaultInlineButton(Emojis.USA.getEmojiSpace() +
                        Language.ENGLISH.name(),
                ProfileState.PROFILE_LANGUAGE_SELECT.name() + Language.ENGLISH);
        return buildInlineMarkup(List.of(List.of(russian, english)));
    }

    public InlineKeyboardMarkup getProfileOptionsMarkup() {
        InlineKeyboardButton timeZone = InlineKeyboardButton.builder()
                .text(Emojis.ALARM_CLOCK.getEmojiSpace() + "Выбрать часовой пояс")
                .switchInlineQueryCurrentChat("").build();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton language =
                defaultInlineButton(Emojis.RUSSIA.getEmojiSpace() + "Выбрать язык (скоро)",
                        ProfileState.PROFILE_LANGUAGE_SELECT.name());

        keyboard.add(List.of(language));
        keyboard.add(List.of(timeZone));

        return buildInlineMarkup(keyboard);
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(CREATE.getButtonName()));
        row1.add(new KeyboardButton(UPCOMING.getButtonName()));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(PREVIOUS.getButtonName()));
        row2.add(new KeyboardButton(PROFILE.getButtonName()));
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .selective(true)
                .build();
    }
}
