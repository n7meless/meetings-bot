package com.ufanet.meetingsbot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReplyKeyboardButton {
    CREATE_MEETING("Создать встречу"),
    UPCOMING_MEETINGS("Предстоящие встречи"),
    EDIT_MEETING("Редактировать встречу"),
    MY_PROFILE("Мой профиль");

    private final String buttonName;
    public static ReplyKeyboardButton fromValue(String text) {
        for (ReplyKeyboardButton value : ReplyKeyboardButton.values()) {
            if (value.equals(text))
                return value;
        }
        return null;
    }
    public boolean equals(String buttonName){
        return this.toString().equals(buttonName);
    }

    @Override
    public String toString() {
        return buttonName;
    }
}
