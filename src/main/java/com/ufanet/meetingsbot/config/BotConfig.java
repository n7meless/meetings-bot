package com.ufanet.meetingsbot.config;

import com.ufanet.meetingsbot.constants.DefaultCommand;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import java.util.ArrayList;
import java.util.List;

@Getter
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BotConfig {
    @Value("${telegram.username}")
    String botUsername;
    @Value("${telegram.botToken}")
    String botToken;

    @Bean
    public SetMyCommands setMyCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand(DefaultCommand.START.getCommand(), "START"));
        commands.add(new BotCommand(DefaultCommand.HELP.getCommand(), "HELP"));
        commands.add(new BotCommand(DefaultCommand.ABOUT.getCommand(), "ABOUT"));
        return new SetMyCommands(commands, new BotCommandScopeDefault(), null);
    }

}
