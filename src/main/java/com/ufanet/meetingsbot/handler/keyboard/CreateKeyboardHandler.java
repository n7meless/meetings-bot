package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.handler.message.ReplyMessageHandler;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.state.AccountState;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class CreateKeyboardHandler implements KeyboardHandler {
    private final MeetingService meetingService;
    private final ReplyMessageHandler meetingReplyMessageHandler;
    private final AccountService accountService;

    public CreateKeyboardHandler(MeetingService meetingService,
                                 @Qualifier("meetingReplyMessageHandler")
                                 ReplyMessageHandler meetingReplyMessageHandler,
                                 AccountService accountService) {
        this.meetingService = meetingService;
        this.meetingReplyMessageHandler = meetingReplyMessageHandler;
        this.accountService = accountService;
    }

    @Override
    public BotApiMethod<?> handleUpdate(Update update) {
        UpdateDto updateDto = UpdateService.parseUpdate(update);
        long chatId = updateDto.chatId();
        String content = updateDto.content();
        handleCallback(chatId, content);
        meetingReplyMessageHandler.replyMessage(chatId, content);
        return null;
    }

    void handleCallback(long userId, String callback) {
        switch (callback) {
            case "next" -> meetingService.setNextState(userId);
            case "cancel" -> meetingService.removeByOwnerId(userId);
            default -> meetingService.update(userId, callback);
        }
    }

    @Override
    public AccountState getUserStateHandler() {
        return AccountState.CREATE;
    }
}
