package com.ufanet.meetingsbot.utils;

import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.model.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.ufanet.meetingsbot.utils.Emojis.*;
import static java.util.stream.Collectors.joining;

@Component
public class MessageUtils {
    public SendMessage generateSendMessage(long chatId, String text) {
        return SendMessage.builder()
                .disableWebPagePreview(true)
                .text(text).chatId(chatId)
                .build();
    }

    public SendMessage generateSendMessage(long chatId, String text, ReplyKeyboard markup) {
        return SendMessage.builder()
                .text(text).chatId(chatId)
                .replyMarkup(markup).build();
    }

    public SendMessage generateSendMessageHtml(long chatId, String text, ReplyKeyboard markup) {
        return SendMessage.builder().parseMode("HTML")
                .disableWebPagePreview(true)
                .text(text).chatId(chatId)
                .replyMarkup(markup).build();
    }

    public EditMessageText generateEditMessageHtml(long chatId, String text,
                                                   InlineKeyboardMarkup markup) {
        return EditMessageText.builder().chatId(chatId).text(text)
                .disableWebPagePreview(true)
                .parseMode("HTML")
                .replyMarkup(markup).build();
    }

    public EditMessageReplyMarkup disableInlineMarkup(long chatId, Integer messageId) {
        return EditMessageReplyMarkup.builder().chatId(chatId).messageId(messageId)
                .replyMarkup(null).build();
    }


    public MeetingMessage generateMeetingMessage(Meeting meeting) {
        List<LocalDateTime> dateTimes = meeting.getDates().stream()
                .map(MeetingDate::getMeetingTimes)
                .flatMap(Collection::stream)
                .map(MeetingTime::getTime)
                .sorted().toList();

        Account meetingOwner = meeting.getOwner();
        String owner = generateAccountLink(meetingOwner, "");

        Set<Account> accounts = meeting.getParticipants();

        accounts.add(meetingOwner);

        String participants = generateAccountLink(accounts, SELECTED.getEmojiSpace());

        accounts.remove(meetingOwner);

        String subject = CLIPBOARD.getEmojiSpace() + meeting.getSubject().getTitle();
        String questions = meeting.getSubject().getQuestions().stream().map(Question::getTitle)
                .collect(joining("\n" + QUESTION.getEmojiSpace(), QUESTION.getEmojiSpace(), "\n"));

        Integer subjectDuration = meeting.getSubject().getDuration();
        String duration = subjectDuration == null ? "(не указано)" : CLOCK.getEmojiSpace() + subjectDuration;
        String times = dateTimes.stream().map(date -> date.format(CustomFormatter.DATE_TIME_WEEK_FORMATTER))
                .collect(joining("\n" + CALENDAR.getEmojiSpace(), CALENDAR.getEmojiSpace(), "\n"));

        String address = meeting.getAddress() == null ? "(не указано)" : OFFICE.getEmojiSpace() + meeting.getAddress();

        return new MeetingMessage(owner, participants, subject,
                questions, duration, times, address);
    }

    public String generateAccountLink(Account account, String delimiter) {
        StringBuilder sb = new StringBuilder();
        if (account.getUsername() == null) {
            sb.append(delimiter).append("<a href='tg://user?id=")
                    .append(account.getId()).append("'>")
                    .append(account.getFirstname()).append("</a>");
        } else {
            sb.append(delimiter).append("<a href='https://t.me/")
                    .append(account.getUsername()).append("'>")
                    .append(account.getFirstname()).append("</a>");
        }
        return sb.toString();
    }

    public String generateAccountLink(Set<Account> accounts, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (Account account : accounts) {
            sb.append(generateAccountLink(account, delimiter));
            sb.append("\n");
        }
        return sb.toString();
    }
}
