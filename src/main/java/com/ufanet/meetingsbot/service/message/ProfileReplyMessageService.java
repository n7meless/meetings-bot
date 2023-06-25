package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.BotCommands;
import com.ufanet.meetingsbot.constants.state.ProfileState;
import com.ufanet.meetingsbot.keyboard.MainKeyboardMaker;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import com.ufanet.meetingsbot.utils.Emojis;
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
public class ProfileReplyMessageService extends ReplyMessageService {
    private final MainKeyboardMaker mainKeyboard;

    public void sendInlineMessageQuery(String queryId) {
        List<InlineQueryResultArticle> articles = new ArrayList<>();

        for (int i = -14; i <= 14; i++) {
            ZoneId zone;

            if (i >= 0) {
                zone = ZoneId.of("UTC+" + i);
            } else {
                zone = ZoneId.of("UTC" + i);
            }

            ZonedDateTime zonedDateTime = ZonedDateTime.now(zone);
            String descriptionText = zonedDateTime.format(CustomFormatter.DATE_TIME_FORMATTER);
            String messageCallback = BotCommands.SETTIMEZONE.getCommand() + " " + zone.getId();

            InputTextMessageContent messageContent = InputTextMessageContent.builder()
                    .messageText(messageCallback).build();

            InlineQueryResultArticle article = InlineQueryResultArticle.builder()
                    .inputMessageContent(messageContent).id(Integer.toString(i))
                    .title(zone.getId()).description(descriptionText)
                    .build();

            articles.add(article);
        }

        AnswerInlineQuery build = AnswerInlineQuery.builder()
                .inlineQueryId(queryId).cacheTime(0).results(articles).build();

        telegramBot.safeExecute(build);
    }

    public void sendProfileMessage(Long chatId) {
        InlineKeyboardButton timeZone = InlineKeyboardButton.builder().text(Emojis.ALARM_CLOCK.getEmojiSpace() + "Выбрать часовой пояс")
                .switchInlineQueryCurrentChat("").build();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton language = mainKeyboard.defaultInlineButton(Emojis.RUSSIA.getEmojiSpace() +  "Выбрать язык",
                ProfileState.PROFILE_LANGUAGE_SELECT.name());

        keyboard.add(List.of(language));
        keyboard.add(List.of(timeZone));

        InlineKeyboardMarkup keyboardMarkup = mainKeyboard.buildInlineMarkup(keyboard);

        SendMessage sendMessage = messageUtils.generateSendMessage(chatId,
                "Выберите нужную опцию:", keyboardMarkup);
        executeSendMessage(sendMessage);
    }

    public void sendSuccessTimezoneSelected(Long userId) {
        SendMessage sendMessage =
                messageUtils.generateSendMessage(userId, "Временная зона успешно выбрана");
        executeSendMessage(sendMessage);
    }
}
