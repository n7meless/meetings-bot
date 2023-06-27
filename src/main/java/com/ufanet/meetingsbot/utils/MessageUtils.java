package com.ufanet.meetingsbot.utils;

import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.model.Question;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.time.ZonedDateTime;
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

        Account meetingOwner = meeting.getOwner();
        String owner = generateAccountLink(meetingOwner, "", "");

        Set<Account> accounts = meeting.getParticipants();

        String participants = generateAccountLink(accounts, GREEN_SELECTED.getEmojiSpace(), "");

        String subject = CLIPBOARD.getEmojiSpace() + meeting.getSubject().getTitle();
        String questions = meeting.getSubject().getQuestions().stream().map(Question::getTitle)
                .collect(joining("\n" + QUESTION.getEmojiSpace(), QUESTION.getEmojiSpace(), "\n"));

        Integer subjectDuration = meeting.getSubject().getDuration();
        String duration = subjectDuration == null ? "(не указано)" : DURATION.getEmojiSpace() + subjectDuration;

        String address = meeting.getAddress() == null ? "(не указано)" : OFFICE.getEmojiSpace() + meeting.getAddress();

        return new MeetingMessage(owner, participants, subject,
                questions, duration, address);
    }
    public String generateDatesText(List<ZonedDateTime> dates) {
        StringBuilder sb = new StringBuilder();
        for (ZonedDateTime zonedDateTime : dates) {
            String formattedDate = zonedDateTime.format(CustomFormatter.DATE_TIME_WEEK_FORMATTER);
            sb.append(CALENDAR.getEmojiSpace()).append(formattedDate).append("\n");
        }
        return sb.toString();
    }

    public String generateAccountLink(Account account, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        if (account.getUsername() == null) {
            sb.append("<a href='tg://user?id=").append(account.getId());
        } else {
            sb.append("<a href='https://t.me/").append(account.getUsername());
        }
        sb.append("'>").append(account.getFirstname()).append("</a>").append(suffix);
        return sb.toString();
    }

    public String generateAccountLink(Set<Account> accounts, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder();
        for (Account account : accounts) {
            sb.append(generateAccountLink(account, prefix, suffix));
            sb.append("\n");
        }
        return sb.toString();
    }

    public String buildText(String ...text){
        StringBuilder sb = new StringBuilder();
        for (String s : text) {
            sb.append(s);
        }
        return sb.toString();
    }
}
