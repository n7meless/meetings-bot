package com.ufanet.meetingsbot.config;

import com.ufanet.meetingsbot.constants.BotCommands;
import com.ufanet.meetingsbot.constants.Emojis;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Setter
@Getter
@Configuration
@RequiredArgsConstructor
public class BotConfig {
    @Value("${telegram.bot.username}")
    private String username;
    @Value("${telegram.bot.token}")
    private String botToken;
    private String webHookPath;

    @Bean
    public SetMyCommands setMyCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand(BotCommands.START.getCommand(), Emojis.ROCKET.getEmojiSpace() + "Запуск"));
        commands.add(new BotCommand(BotCommands.HELP.getCommand(), Emojis.HISTORY.getEmojiSpace() + "Инструкция"));
        commands.add(new BotCommand(BotCommands.ABOUT.getCommand(), Emojis.INFO.getEmojiSpace() + "О боте"));
        return new SetMyCommands(commands, new BotCommandScopeDefault(), null);
    }

    @Bean
    @Profile("webhook")
    public SetWebhook setWebhook(@Value("${telegram.bot.webHookPath}")
                                 String webHookPath) {
        this.webHookPath = webHookPath;
        return SetWebhook.builder().url(webHookPath).build();
    }
}
