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
        String owner;

        if (meetingOwner.getUsername() == null) {
            owner = "<a href='tg://user?id=" + meetingOwner.getId() + "'>" + meetingOwner.getFirstname() + "</a>";
        } else {
            owner = "<a href='https://t.me/" + meetingOwner.getUsername() + "'>" + meetingOwner.getFirstname() + "</a>";
        }
        Set<Account> accounts = meeting.getParticipants();

        accounts.add(meetingOwner);
        StringBuilder sb = new StringBuilder();
        for (Account account : accounts) {
            if (account.getUsername() == null) {
                sb.append(SELECTED.getEmojiSpace()).append("<a href='tg://user?id=")
                        .append(account.getId()).append("'>")
                        .append(account.getFirstname()).append("</a>");
            } else {
                sb.append(SELECTED.getEmojiSpace()).append("<a href='https://t.me/")
                        .append(account.getUsername()).append("'>")
                        .append(account.getFirstname()).append("</a>");
            }
            sb.append("\n");
        }
        String participants = sb.toString();
        accounts.remove(meetingOwner);

//        accounts.add(meetingOwner);
//        String participants = accounts.stream()
//                .map(account -> "<a href='tg://user?id=" + account.getId() + "'>" + account.getFirstname() + "</a>")
//                .collect(joining("\n" + SELECTED.getEmojiSpace(), SELECTED.getEmojiSpace(), "\n"));
//        accounts.remove(meetingOwner);

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
}
