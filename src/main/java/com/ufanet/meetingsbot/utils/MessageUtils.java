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


    public MeetingMessage generateMeetingMessage(Meeting meeting) {
        List<LocalDateTime> dateTimes = meeting.getDates().stream()
                .map(MeetingDate::getMeetingTimes).flatMap(Collection::stream)
                .map(MeetingTime::getTime)
                .sorted(LocalDateTime::compareTo).toList();
        Account meetingOwner = meeting.getOwner();

        String owner = "<a href='https://t.me/" + meetingOwner.getUsername() + "'>" + meetingOwner.getFirstname() + "</a>";

        Set<Account> accounts = meeting.getParticipants();

        accounts.add(meetingOwner);
        String participants = accounts.stream()
                .map(account -> "<a href='https://t.me/" + account.getUsername() + "'>" + account.getFirstname() + "</a>")
                .collect(joining("\n" + SELECTED.getEmojiSpace(), SELECTED.getEmojiSpace(), "\n"));
        accounts.remove(meetingOwner);

        String subject = CLIPBOARD.getEmojiSpace() + meeting.getSubject().getTitle();
        String questions = meeting.getSubject().getQuestions().stream().map(Question::getTitle)
                .collect(joining("\n" + QUESTION.getEmojiSpace(), QUESTION.getEmojiSpace(), "\n"));

        String duration = CLOCK.getEmojiSpace() + meeting.getSubject().getDuration();
        String times = dateTimes.stream().map(date -> date.format(CustomFormatter.DATE_TIME_WEEK_FORMATTER))
                .collect(joining("\n" + CALENDAR.getEmojiSpace(), CALENDAR.getEmojiSpace(), "\n"));

        String address = OFFICE.getEmojiSpace() + meeting.getAddress();

        return new MeetingMessage(owner, participants, subject,
                questions, duration, times, address);
    }
}
