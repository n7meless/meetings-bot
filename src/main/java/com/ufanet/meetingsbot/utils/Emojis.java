package com.ufanet.meetingsbot.utils;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Emojis {
    SELECTED(EmojiParser.parseToUnicode(":white_check_mark:")),
    PIN(EmojiParser.parseToUnicode(":pushpin:")),
    ALARM_CLOCK(EmojiParser.parseToUnicode(":alarm_clock:")),
    CLOCK(EmojiParser.parseToUnicode(":clock1:")),
    OFFICE(EmojiParser.parseToUnicode(":office:")),
    CALENDAR(EmojiParser.parseToUnicode(":date:")),
    CLIPBOARD(EmojiParser.parseToUnicode(":clipboard:")),
    QUESTION(EmojiParser.parseToUnicode(":grey_question:"));


    private final String emoji;
    @Override
    public String toString() {
        return emoji;
    }
}
