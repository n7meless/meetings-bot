package com.ufanet.meetingsbot.config;

import com.ufanet.meetingsbot.service.TelegramFacade;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;

@Slf4j
@Getter
@Setter
@Component
@Profile("webhook")
public class WebhookTelegramBot extends SpringWebhookBot {
    private String botPath;
    private String botUsername;

    private final TelegramFacade telegramFacade;

    public WebhookTelegramBot(TelegramFacade telegramFacade, SetWebhook setWebhook,
                              BotConfig botConfig, SetMyCommands setMyCommands) {
        super(setWebhook, botConfig.getBotToken());
        this.botPath = botConfig.getWebHookPath();
        this.botUsername = botConfig.getUsername();
        this.telegramFacade = telegramFacade;
        this.safeExecute(setMyCommands);
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        telegramFacade.handleUpdate(update);
        return null;
    }

    public void safeExecute(BotApiMethod<?> method) {
        try {
            this.execute(method);
        } catch (TelegramApiException e) {
            log.error("an occurred error when execute method");
        }
    }
    @Bean
    public CommandLineRunner registerBot(@Value("${telegram.bot.authorizePath}") String authorizePath,
                                               RestTemplate restTemplate) {
        return (c) -> {
            log.info("trying register bot @{} on webhook", botUsername);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(authorizePath, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                log.info("bot authorized successfully");
            } else {
                log.error("can not authorize bot in telegram");
                throw new RuntimeException();
            }
        };
    }
}