package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.BotCommands;
import com.ufanet.meetingsbot.keyboard.MainKeyboardMaker;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandReplyMessageService extends ReplyMessageService {
    private final MainKeyboardMaker mainKeyboard;
    private final String[] rainbowEmotions = {"\uD83D\uDD34", "\uD83D\uDFE0",
            "\uD83D\uDFE1", "\uD83D\uDFE2", "\uD83D\uDD35", "\uD83D\uDFE3"};

    public void sendAboutMessage(long userId) {
        SendMessage aboutMessage = messageUtils.generateSendMessage(userId,
                "Здесь написано обо мне");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (String smile : rainbowEmotions) {
            InlineKeyboardButton build = InlineKeyboardButton.builder()
                    .text(smile).callbackData(" ").build();
            buttons.add(build);
        }
        markup.setKeyboard(List.of(buttons));
        aboutMessage.setReplyMarkup(markup);
        telegramBot.safeExecute(aboutMessage);
    }

    public void sendLanguageSelectMessage(long userId) {
        SendMessage startMessage = messageUtils.generateSendMessage(userId,
                "Добро пожаловать!", mainKeyboard.getMainMenuKeyboard());
        telegramBot.safeExecute(startMessage);
    }

    public void sendHelpMessage(long userId) {
        SendMessage helpMessage = messageUtils.generateSendMessage(userId,
                "Здесь я вам помогу");
        telegramBot.safeExecute(helpMessage);
    }

}
