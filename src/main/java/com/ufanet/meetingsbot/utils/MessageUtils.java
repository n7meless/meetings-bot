package com.ufanet.meetingsbot.utils;

import com.ufanet.meetingsbot.model.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Component
public class MessageUtils {
    public SendMessage generateSendMessage(long chatId, String text) {
        return SendMessage.builder()
                .text(text).chatId(chatId)
                .build();
    }

    public SendMessage generateSendMessage(long chatId, String text, ReplyKeyboard markup) {
        return SendMessage.builder()
                .text(text).chatId(chatId)
                .replyMarkup(markup).build();
    }

    public EditMessageText generateEditMessage(long chatId, String text,
                                               Integer messageId, InlineKeyboardMarkup markup) {
        return EditMessageText.builder().chatId(chatId)
                .messageId(messageId).text(text)
                .replyMarkup(markup).build();
    }

    public EditMessageReplyMarkup disableInlineMarkup(long chatId, Integer messageId) {
        return EditMessageReplyMarkup.builder().chatId(chatId).messageId(messageId)
                .replyMarkup(null).build();
    }

    public SendMessage generateMeeting(long chatId, Meeting meeting, InlineKeyboardMarkup markup) {
        StringBuilder sb = new StringBuilder();
        sb.append("Вы собираетесь создать встречу с этими участниками:");
        Set<Account> participants = meeting.getParticipants();
        for (Account participant : participants) {
            sb.append("\n" + Emojis.SELECTED.getEmoji() + " " + participant.getFirstname() + " (@" + participant.getUsername() + ")");
        }
        Subject subject = meeting.getSubject();
        List<Question> questions = subject.getQuestions();
        sb.append("\n");
        sb.append("\nТема для обсуждения:");
        sb.append("\n " + Emojis.CLIPBOARD.getEmoji() + " " + subject.getTitle());
        sb.append("\n");
        sb.append("\nОбсуждаемые вопросы встречи:");
        for (Question question : questions) {
            sb.append("\n" + Emojis.QUESTION.getEmoji() + question.getTitle());
        }
        sb.append("\n");
        sb.append("\nОбщее ожидаемое время для запланированной встречи:");
        sb.append("\n" + Emojis.CLOCK.getEmoji() + " " + subject.getDuration() + " мин.");
        sb.append("\n");
        sb.append("\nВы выбрали эти возможные интервалы для встречи:");
        List<MeetingDate> dates = meeting.getDates();
        for (MeetingDate date : dates) {
            List<MeetingTime> times = date.getTime();
            for (MeetingTime time : times) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                LocalDateTime dateTime = LocalDateTime.of(date.getDate(), time.getTime());
                String formatDateTime = dateTime.format(formatter);
                sb.append("\n" + Emojis.CALENDAR.getEmoji() + " " + formatDateTime);
            }
        }
        sb.append("\n");
        sb.append("\nВстреча пройдет по этому адресу:");
        sb.append("\n" + Emojis.OFFICE.getEmoji() + " " + meeting.getAddress());
        return SendMessage.builder().text(sb.toString()).replyMarkup(markup).chatId(chatId).build();
    }
}
