package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.BotCommands;
import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Settings;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.message.ProfileReplyMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;

import static com.ufanet.meetingsbot.constants.state.AccountState.PROFILE;

@Component
@RequiredArgsConstructor
public class ProfileKeyboardHandler implements KeyboardHandler {
    private final ProfileReplyMessageService profileReplyMessage;
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

    private void handleMessage(Message message) {
        String messageText = message.getText();
        Long userId = message.getChatId();
        if (messageText.equals(PROFILE.getButtonName())) {
            profileReplyMessage.sendProfileMessage(userId);
        }else if (messageText.startsWith(BotCommands.SETTIMEZONE.getCommand())){
            String timeZone = messageText.split(" ")[1];
            System.out.println(timeZone);
            Account account = accountService.getByUserId(userId).orElseThrow();
            Settings settings = account.getSettings();
            settings.setTimeZone(timeZone);
            account.setSettings(settings);
            accountService.save(account);
            profileReplyMessage.sendSuccessTimezoneSelected(userId);
        }
    }

    private void handleInlineQuery(InlineQuery inlineQuery) {
        String queryId = inlineQuery.getId();
        profileReplyMessage.sendInlineMessageQuery(queryId);
    }

    void handleCallback(CallbackQuery callbackQuery) {
        User tgUser = callbackQuery.getFrom();
        String callback = callbackQuery.getData();
    }

    @Override
    public AccountState getAccountStateHandler() {
        return PROFILE;
    }
}
