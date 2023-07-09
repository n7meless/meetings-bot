package com.ufanet.meetingsbot.utils;

import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static com.ufanet.meetingsbot.constants.Emojis.*;
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
                .disableWebPagePreview(true)
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

    public EditMessageText generateEditMessageHtml(long chatId, int messageId, String text,
                                                   InlineKeyboardMarkup markup) {
        return EditMessageText.builder().chatId(chatId).text(text)
                .disableWebPagePreview(true)
                .parseMode("HTML")
                .messageId(messageId)
                .replyMarkup(markup).build();
    }

    public MeetingMessage generateMeetingMessage(MeetingDto meetingDto) {

        AccountDto accountDto = meetingDto.getOwner();
        String owner = generateAccountLink(accountDto, "", "");

        Set<AccountDto> accounts = meetingDto.getParticipants();

        String participants = generateAccountLink(accounts, GREEN_SELECTED.getEmojiSpace(), "");

        String subject = CLIPBOARD.getEmojiSpace() + meetingDto.getSubjectDto().getTitle();
        String questions = meetingDto.getSubjectDto().getQuestions().stream()
                .collect(joining("\n" + QUESTION.getEmojiSpace(), QUESTION.getEmojiSpace(), "\n"));

        int subjectDuration = meetingDto.getSubjectDto().getDuration();
        String duration = subjectDuration == 0 ? "(не указано)" : DURATION.getEmojiSpace() + subjectDuration;

        String address = meetingDto.getAddress() == null ? "(не указано)" : OFFICE.getEmojiSpace() + meetingDto.getAddress();

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

    public String generateAccountLink(AccountDto accountDto, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        if (accountDto.getUsername() == null) {
            sb.append("<a href='tg://user?id=").append(accountDto.getId());
        } else {
            sb.append("<a href='https://t.me/").append(accountDto.getUsername());
        }
        sb.append("'>").append(accountDto.getFirstname()).append("</a>").append(suffix);
        return sb.toString();
    }

    public String generateAccountLink(Set<AccountDto> accounts, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder();
        for (AccountDto account : accounts) {
            sb.append(generateAccountLink(account, prefix, suffix));
            sb.append("\n");
        }
        return sb.toString();
    }

    public String buildText(String... text) {
        StringBuilder sb = new StringBuilder();
        for (String s : text) {
            sb.append(s);
        }
        return sb.toString();
    }
}
