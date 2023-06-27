package com.ufanet.meetingsbot.controller;

import com.ufanet.meetingsbot.service.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
public class WebhookController {
    private final TelegramBot telegramBot;
    @PostMapping("/")
    public ResponseEntity<?> onUpdateReceived(@RequestBody Update update) {
        telegramBot.onWebhookUpdateReceived(update);
        return ResponseEntity.ok().build();
    }
}
