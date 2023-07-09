package com.ufanet.meetingsbot.constants.type;

import com.ufanet.meetingsbot.constants.Emojis;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
    CREATE(Emojis.BELL.getEmojiSpace() + "Создать встречу"),
    UPCOMING(Emojis.CALENDAR.getEmojiSpace() + "Предстоящие встречи"),
    PREVIOUS(Emojis.HISTORY.getEmojiSpace() + "История встреч"),
    EDIT(""),
    PROFILE(Emojis.PROFILE.getEmojiSpace() + "Мой профиль");

    private final String buttonName;

    public static EventType fromValue(String text) {
        for (EventType value : EventType.values()) {
            if (value.equals(text))
                return value;
        }
        return null;
    }

    public static boolean startWithState(String text) {
        if (text.startsWith(CREATE.name())) {
            return true;
        } else if (text.startsWith(UPCOMING.name())) {
            return true;
        } else if (text.startsWith(PREVIOUS.name())) {
            return true;
        } else if (text.startsWith(PROFILE.name())) {
            return true;
        } else return text.startsWith(EDIT.name());
    }

    public boolean equals(String buttonName) {
        return this.toString().equals(buttonName);
    }

    @Override
    public String toString() {
        return buttonName;
    }
}
