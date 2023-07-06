package com.ufanet.meetingsbot.handler.event.impl;

import com.ufanet.meetingsbot.constants.BotCommands;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.entity.Settings;
import com.ufanet.meetingsbot.exceptions.AccountNotFoundException;
import com.ufanet.meetingsbot.exceptions.ValidationException;
import com.ufanet.meetingsbot.handler.event.EventHandler;
import com.ufanet.meetingsbot.message.ProfileReplyMessage;
import com.ufanet.meetingsbot.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;

import static com.ufanet.meetingsbot.constants.state.AccountState.PROFILE;

@Service
@RequiredArgsConstructor
public class ProfileEventHandler implements EventHandler {
    private final ProfileReplyMessage profileReplyMessage;
    private final AccountService accountService;

    @Override
    public void handleUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        } else if (update.hasInlineQuery()) {
            handleInlineQuery(update.getInlineQuery());
        } else if (update.hasMessage()) {
            handleMessage(update.getMessage());
        }
    }

    protected void handleMessage(Message message) {
        String messageText = message.getText();
        Long userId = message.getChatId();
        if (messageText.equals(PROFILE.getButtonName())) {
            profileReplyMessage.sendProfileMessage(userId);
        } else if (messageText.startsWith(BotCommands.SETTIMEZONE.getCommand())) {
            String timeZone = messageText.substring(BotCommands.SETTIMEZONE.getCommand().length() + 1);

            if (timeZone.startsWith("UTC")) {
                System.out.println(timeZone);
                Account account = accountService.getByUserId(userId)
                        .orElseThrow(() -> new AccountNotFoundException(userId));
                Settings settings = account.getSettings();
                settings.setTimeZone(timeZone);
                account.setSettings(settings);
                accountService.save(account);
                profileReplyMessage.sendSuccessTimezoneSelected(userId);
            } else throw new ValidationException(userId,
                    "error.validation.timezone");
        }
    }

    protected void handleInlineQuery(InlineQuery inlineQuery) {
        String queryId = inlineQuery.getId();
        profileReplyMessage.sendInlineMessageQuery(queryId);
    }

    protected void handleCallback(CallbackQuery callbackQuery) {
        //TODO language select
        User tgUser = callbackQuery.getFrom();
        String callback = callbackQuery.getData();
    }

    @Override
    public AccountState getAccountStateHandler() {
        return PROFILE;
    }
}
