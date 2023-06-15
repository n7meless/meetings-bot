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

    public String generateAwaitingMeeting(Meeting meeting) {
        StringBuilder sb = new StringBuilder();
        sb.append("Вы собираетесь создать встречу с этими участниками:\n");
        Set<Account> participants = meeting.getParticipants();
        for (Account participant : participants) {
            sb.append(Emojis.SELECTED.getEmoji() + " [" + participant.getFirstname() + "](https://t.me/" + participant.getUsername() + ")");
            sb.append("\n");
        }
        Subject subject = meeting.getSubject();
        List<Question> questions = subject.getQuestions();
        sb.append("\nТема для обсуждения:\n");
        sb.append(Emojis.CLIPBOARD.getEmoji() + " " + subject.getTitle());
        sb.append("\n");
        sb.append("\nОбсуждаемые вопросы встречи:\n");
        for (Question question : questions) {
            sb.append(Emojis.QUESTION.getEmoji() + question.getTitle());
            sb.append("\n");
        }
        sb.append("\nОбщее ожидаемое время для запланированной встречи:\n");
        sb.append(Emojis.CLOCK.getEmoji() + " " + subject.getDuration() + " мин.");
        sb.append("\n");
        sb.append("\nВы выбрали эти возможные интервалы для встречи:\n");
        List<MeetingDate> dates = meeting.getDates();
        for (MeetingDate date : dates) {
            List<MeetingTime> times = date.getTime();
            for (MeetingTime time : times) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

                LocalDateTime dateTime = LocalDateTime.of(date.getDate(), time.getTime());
                String formatDateTime = dateTime.format(formatter);
                sb.append(Emojis.CALENDAR.getEmoji() + " " + formatDateTime);
                sb.append("\n");
            }
        }
        sb.append("\nВстреча пройдет по этому адресу:\n");
        sb.append(Emojis.OFFICE.getEmoji() + " " + meeting.getAddress());
        return sb.toString().replace(".", "\\.");
    }

    public String generateTextMeeting(Meeting meeting) {

        StringBuilder sb = new StringBuilder();
        Account owner = meeting.getOwner();
        sb.append("Ваш коллега [" + owner.getFirstname() + "](https://t.me/" + owner.getUsername() + ") приглашает вас на встречу для обсуждения темы: \n");
        Subject subject = meeting.getSubject();
        List<Question> questions = subject.getQuestions();
        sb.append("\n");
        sb.append(Emojis.CLIPBOARD.getEmoji() + " " + subject.getTitle());
        sb.append("\n");
        sb.append("\nГде будут обсуждаться такие вопросы как:\n");
        for (Question question : questions) {
            sb.append(Emojis.QUESTION.getEmoji() + question.getTitle());
            sb.append("\n");
        }
        sb.append("\nВместе с вашими коллегами:\n");
        Set<Account> participants = meeting.getParticipants();
        for (Account participant : participants) {
            sb.append(Emojis.SELECTED.getEmoji() + " [" + participant.getFirstname() + "](@" + participant.getUsername() + ")");
            sb.append("\n");
        }
        sb.append("\nОбщее ожидаемое время для запланированной встречи:\n");
        sb.append(Emojis.CLOCK.getEmoji() + " " + subject.getDuration() + " мин");
        sb.append("\n");
        sb.append("\nВыбранные интервалы времени, которые вам подходят:\n");
        List<MeetingDate> dates = meeting.getDates();
        for (MeetingDate date : dates) {
            List<MeetingTime> times = date.getTime();
            for (MeetingTime time : times) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

                LocalDateTime dateTime = LocalDateTime.of(date.getDate(), time.getTime());
                String formatDateTime = dateTime.format(formatter);
                sb.append(Emojis.CALENDAR.getEmoji() + " " + formatDateTime);
                sb.append("\n");
            }
        }
        sb.append("\nВстреча пройдет по этому адресу:\n");
        sb.append(Emojis.OFFICE.getEmoji() + " " + meeting.getAddress());
        return sb.toString().replace(".", "\\.");
    }
}
