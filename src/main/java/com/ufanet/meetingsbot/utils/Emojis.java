package com.ufanet.meetingsbot.utils;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Emojis {
    SELECTED(EmojiParser.parseToUnicode(":white_check_mark:")),
    CHANGE(EmojiParser.parseToUnicode(":memo:")),
    RED_CIRCLE(EmojiParser.parseToUnicode(":x:")),
    PIN(EmojiParser.parseToUnicode(":pushpin:")),
    ALARM_CLOCK(EmojiParser.parseToUnicode(":alarm_clock:")),
    CLOCK(EmojiParser.parseToUnicode(":clock1:")),
    OFFICE(EmojiParser.parseToUnicode(":office:")),
    CALENDAR(EmojiParser.parseToUnicode(":date:")),
    CLIPBOARD(EmojiParser.parseToUnicode(":clipboard:")),
    QUESTION(EmojiParser.parseToUnicode(":grey_question:"));


    private final String emoji;

    public String getEmoji() {
        return emoji;
    }
    public String getEmojiSpace(){
        return emoji + " ";
    }

    @Override
    public String toString() {
        return emoji;
    }
}
