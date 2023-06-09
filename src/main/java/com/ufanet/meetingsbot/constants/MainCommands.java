package com.ufanet.meetingsbot.constants;

import lombok.Getter;

@Getter
public enum MainCommands {

    START("/start"),
    HELP("/help");

    private final String command;

    MainCommands(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return this.getCommand();
    }
}
