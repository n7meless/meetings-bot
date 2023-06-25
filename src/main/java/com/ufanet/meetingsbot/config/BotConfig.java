package com.ufanet.meetingsbot.config;

import com.ufanet.meetingsbot.constants.BotCommands;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
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
    @Value("${telegram.bot.webHookPath}")
    String webHookPath;

    @Bean
    public SetMyCommands setMyCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand(BotCommands.START.getCommand(), "START"));
        commands.add(new BotCommand(BotCommands.HELP.getCommand(), "HELP"));
        commands.add(new BotCommand(BotCommands.ABOUT.getCommand(), "ABOUT"));
        return new SetMyCommands(commands, new BotCommandScopeDefault(), null);
    }

}
