package com.ufanet.meetingsbot.constants.state;

import com.ufanet.meetingsbot.utils.Emojis;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountState {
    CREATE_MEETING(Emojis.BELL.getEmojiSpace() + "Создать встречу"),
    UPCOMING_MEETINGS(Emojis.CALENDAR.getEmojiSpace() + "Предстоящие встречи"),
    EDIT_MEETING(Emojis.CHANGE.getEmojiSpace() + "Редактировать встречу"),
    PROFILE_SETTINGS(Emojis.PROFILE.getEmojiSpace() + "Мой профиль");

    private final String buttonName;

    public static AccountState fromValue(String text) {
        for (AccountState value : AccountState.values()) {
            if (value.equals(text))
                return value;
        }
        return null;
    }

    public boolean equals(String buttonName) {
        return this.toString().equals(buttonName);
    }

    @Override
    public String toString() {
        return buttonName;
    }
}
