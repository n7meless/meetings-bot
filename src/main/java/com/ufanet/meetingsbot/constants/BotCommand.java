package com.ufanet.meetingsbot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BotCommand {

    START("/start"),
    HELP("/help"),
    ABOUT("/about");

    private final String command;

    public static boolean typeOf(String command) {
        BotCommand[] values = BotCommand.values();
        for (BotCommand value : values) {
            if (value.getCommand().equals(command)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getCommand();
    }
}
