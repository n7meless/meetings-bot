package com.ufanet.meetingsbot;

import com.ufanet.meetingsbot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@EnableCaching
@SpringBootApplication
public class MeetingsBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(MeetingsBotApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(BotConfig botConfig) {
        return (c) -> {
            String url = String.format("https://api.telegram.org/bot%s/setWebhook?url=%s",
                    botConfig.getBotToken(), botConfig.getWebHookPath());
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                log.info("bot '@{}' authorized successfully", botConfig.getUsername());
            } else {
                log.error("can not authorize bot '@{}' in telegram", botConfig.getUsername());
            }
        };
    }
}