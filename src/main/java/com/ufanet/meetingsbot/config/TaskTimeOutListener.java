package com.ufanet.meetingsbot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskTimeOutListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody());
        String channel = new String(message.getChannel());
        log.info("Received task timeout event: {} for key: {}", body, channel);
        String expiredKey = channel.split(":")[1];
        log.info(expiredKey);
        //implemented logic to discard task
    }
}