package com.ufanet.meetingsbot.config;

import com.ufanet.meetingsbot.constants.BotCommand;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import java.util.ArrayList;
import java.util.List;
@Setter
@Getter
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BotConfig {
    @Value("${telegram.bot.username}")
    String username;
    @Value("${telegram.bot.token}")
    String botToken;

    @Bean
    public SetMyCommands setMyCommands() {
        List<org.telegram.telegrambots.meta.api.objects.commands.BotCommand> commands = new ArrayList<>();
        commands.add(new org.telegram.telegrambots.meta.api.objects.commands.BotCommand(BotCommand.START.getCommand(), "START"));
        commands.add(new org.telegram.telegrambots.meta.api.objects.commands.BotCommand(BotCommand.HELP.getCommand(), "HELP"));
        commands.add(new org.telegram.telegrambots.meta.api.objects.commands.BotCommand(BotCommand.ABOUT.getCommand(), "ABOUT"));
        return new SetMyCommands(commands, new BotCommandScopeDefault(), null);
    }

}
