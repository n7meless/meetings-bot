package com.ufanet.meetingsbot.config;

import com.ufanet.meetingsbot.service.TelegramFacade;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Getter
@Setter
@Component
@Profile("polling")
public class PollingTelegramBot extends TelegramLongPollingBot {
    private String botUsername;
    private final TelegramFacade telegramFacade;

    public PollingTelegramBot(TelegramFacade telegramFacade, BotConfig botConfig,
                              SetMyCommands setMyCommands) {
        super(botConfig.getBotToken());
        this.botUsername = botConfig.getUsername();
        this.telegramFacade = telegramFacade;
        this.safeExecute(setMyCommands);
    }

    @Override
    public void onUpdateReceived(Update update) {
        telegramFacade.handleUpdate(update);
    }

    public void safeExecute(BotApiMethod<?> method) {
        try {
            this.execute(method);
        } catch (TelegramApiException e) {
            log.error("an occurred error when execute method");
        }
    }

    @EventListener({ContextRefreshedEvent.class})
    public void registerBot() {
        try {
            log.info("trying register bot @{} on long polling", botUsername);
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
            log.info("bot authorized successfully");
        } catch (TelegramApiException e) {
            log.error("can not authorize bot in telegram");
            throw new RuntimeException();
        }
    }
}
