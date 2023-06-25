package com.ufanet.meetingsbot.utils;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Emojis {
    GREEN_SELECTED(EmojiParser.parseToUnicode(":white_check_mark:")),
    GREY_SELECTED(EmojiParser.parseToUnicode(":ballot_box_with_check:")),
    CHANGE(EmojiParser.parseToUnicode(":memo:")),
    PROFILE(EmojiParser.parseToUnicode(":necktie:")),
    BELL(EmojiParser.parseToUnicode(":bell:")),
    CANCEL_X(EmojiParser.parseToUnicode(":x:")),
    PIN(EmojiParser.parseToUnicode(":pushpin:")),
    ALARM_CLOCK(EmojiParser.parseToUnicode(":alarm_clock:")),
    CLOCK(EmojiParser.parseToUnicode(":clock1:")),
    OFFICE(EmojiParser.parseToUnicode(":office:")),
    CALENDAR(EmojiParser.parseToUnicode(":date:")),
    CLIPBOARD(EmojiParser.parseToUnicode(":clipboard:")),
    QUESTION(EmojiParser.parseToUnicode(":grey_question:")),
    PARTICIPANTS(EmojiParser.parseToUnicode(":busts_in_silhouette:")),
    MESSAGE(EmojiParser.parseToUnicode(":envelope:")),
    CANCEL_CIRCLE(EmojiParser.parseToUnicode(":no_entry_sign:")),
    CROWN(EmojiParser.parseToUnicode(":crown:")),
    RUSSIA(EmojiParser.parseToUnicode(":ru:")),
    USA(EmojiParser.parseToUnicode(":en:")),
    HISTORY(EmojiParser.parseToUnicode(":page_with_curl:"));

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
