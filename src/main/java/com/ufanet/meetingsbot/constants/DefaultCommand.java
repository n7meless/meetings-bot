package com.ufanet.meetingsbot.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DefaultCommand {

    START("/start"),
    HELP("/help"),
    ABOUT("/about");

    private final String command;

    public static boolean isCommand(String command) {
        DefaultCommand[] values = DefaultCommand.values();
        for (DefaultCommand value : values) {
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
