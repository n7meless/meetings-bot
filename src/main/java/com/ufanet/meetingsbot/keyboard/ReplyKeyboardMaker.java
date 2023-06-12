package com.ufanet.meetingsbot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static com.ufanet.meetingsbot.constants.ReplyKeyboardButton.*;
import static com.ufanet.meetingsbot.constants.ReplyKeyboardButton.MY_PROFILE;
@Component
public class ReplyKeyboardMaker {
    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(CREATE_MEETING.getButtonName()));
        row1.add(new KeyboardButton(UPCOMING_MEETINGS.getButtonName()));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(EDIT_MEETING.getButtonName()));
        row2.add(new KeyboardButton(MY_PROFILE.getButtonName()));
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2))
                .selective(true)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
    }
}
