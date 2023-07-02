package com.ufanet.meetingsbot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotInitializer {
    private final PollingTelegramBot telegramBot;

    @Profile("polling")
    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot((LongPollingBot) telegramBot);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @Bean
    @Profile("webhook")
    public CommandLineRunner commandLineRunner(@Value("${telegram.bot.authorizePath}") String authorizePath,
                                               RestTemplate restTemplate) {
        return (c) -> {
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