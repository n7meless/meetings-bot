package com.ufanet.meetingsbot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BotCommands {

    START("/start"),
    HELP("/help"),
    ABOUT("/about"),
    SETTIMEZONE("/settimezone");

    private final String command;

    public static boolean isCommand(String command) {
        BotCommands[] values = BotCommands.values();
        for (BotCommands value : values) {
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
