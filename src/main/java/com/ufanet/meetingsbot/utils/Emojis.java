package com.ufanet.meetingsbot.utils;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Emojis {
    SELECTED(EmojiParser.parseToUnicode(":white_check_mark:"));

    private final String emojiName;
    @Override
    public String toString() {
        return emojiName;
    }
}
