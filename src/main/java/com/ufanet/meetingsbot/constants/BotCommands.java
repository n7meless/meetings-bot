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
}
