package com.ufanet.meetingsbot.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
@Getter
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BotConfig {
    @Value("${telegram.username}")
    String botUsername;
    @Value("${telegram.botToken}")
    String botToken;
}
