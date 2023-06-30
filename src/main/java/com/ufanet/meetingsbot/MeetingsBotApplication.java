package com.ufanet.meetingsbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
@EnableCaching
@SpringBootApplication
@RequiredArgsConstructor
public class MeetingsBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingsBotApplication.class, args);
    }

    @Bean
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