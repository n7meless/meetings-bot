package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.keyboard.CalendarKeyboardMaker;
import com.ufanet.meetingsbot.keyboard.MeetingKeyboardMaker;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.GroupRepository;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.GroupService;
import com.ufanet.meetingsbot.utils.Emojis;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MeetingReplyMessageService extends ReplyMessageService {
    private final MeetingKeyboardMaker meetingKeyboard;
    private final CalendarKeyboardMaker calendarKeyboard;
    private final AccountService accountService;
    private final GroupService groupService;

    public void sendGroupMessage(long userId, Meeting meeting) {
        List<Group> groups = groupService.getGroupsByMemberId(userId);

        EditMessageText editMessage =
                messageUtils.generateEditMessageHtml(userId, localeMessageService.getMessage("create.meeting.group"),
                        meetingKeyboard.getGroupsInlineMarkup(meeting, groups));

        executeMessage(editMessage);
    }

    public void sendParticipantsMessage(long userId, Meeting meeting) {
        Set<Account> members = accountService.getAccountByGroupsIdAndIdNot(meeting.getGroup().getId(), userId);

        Set<Account> participants = meeting.getParticipants();
        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getParticipantsInlineButtons(members, participants);
        keyboard.add(meetingKeyboard.defaultRowHelperInlineButtons(participants.size() > 0));

        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("create.meeting.participants"),
                meetingKeyboard.buildInlineMarkup(keyboard));

        executeMessage(message);
    }

    public void sendSubjectMessage(long userId, Meeting meeting) {
        Subject subject = meeting.getSubject();
        InlineKeyboardMarkup keyboardMarkup;
        keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(meetingKeyboard.defaultRowHelperInlineButtons(subject != null))
                .build();

        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, "Укажите тему встречи",
                keyboardMarkup);
        executeMessage(editMessage);
    }

    public void sendQuestionMessage(long userId, Meeting meeting) {
        Subject subject = meeting.getSubject();
        Set<String> questions = subject.getQuestions();
        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getQuestionsInlineMarkup(meeting);
        keyboard.add(meetingKeyboard.defaultRowHelperInlineButtons(questions.size() > 0));
        EditMessageText editMessage =
                messageUtils.generateEditMessageHtml(userId, localeMessageService.getMessage("create.meeting.question"),
                        meetingKeyboard.buildInlineMarkup(keyboard));
        executeMessage(editMessage);
    }

    public void sendDateMessage(long userId, Meeting meeting, String callback) {
        Account account = accountService.getByUserId(userId).orElseThrow();
        String zoneId = account.getZoneId();
        List<List<InlineKeyboardButton>> keyboard = calendarKeyboard.getCalendarInlineMarkup(meeting, callback, zoneId);
        keyboard.add(meetingKeyboard.defaultRowHelperInlineButtons(meeting.getDates().size() > 0));
        EditMessageText message =
                messageUtils.generateEditMessageHtml(userId, localeMessageService.getMessage("create.meeting.date"),
                        meetingKeyboard.buildInlineMarkup(keyboard));

        executeMessage(message);
    }

    public void sendTimeMessage(long userId, Meeting meeting) {
        Account account = accountService.getByUserId(userId).orElseThrow();
        String zoneId = account.getZoneId();
        List<List<InlineKeyboardButton>> keyboard = calendarKeyboard.getTimeInlineMarkup(meeting, zoneId);
        boolean hasTime = meeting.getDates().stream().anyMatch(t -> t.getMeetingTimes().size() > 0);
        keyboard.add(meetingKeyboard.defaultRowHelperInlineButtons(hasTime));

        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("create.meeting.time"),
                meetingKeyboard.buildInlineMarkup(keyboard));

        executeMessage(message);
    }

    public void sendAddressMessage(long userId) {
        List<InlineKeyboardButton> buttons =
                meetingKeyboard.defaultRowHelperInlineButtons(true);

        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("create.meeting.address"),
                InlineKeyboardMarkup.builder().keyboardRow(buttons).build());

        executeMessage(message);
    }

    public void sendSubjectDurationMessage(long userId, Meeting meeting) {
        Subject subject = meeting.getSubject();
        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getSubjectDurationInlineMarkup(meeting);
        keyboard.add(meetingKeyboard.defaultRowHelperInlineButtons(subject.getDuration() != null));

        EditMessageText editMessage =
                messageUtils.generateEditMessageHtml(userId,
                        localeMessageService.getMessage("create.meeting.duration"),
                        meetingKeyboard.buildInlineMarkup(keyboard));
        executeMessage(editMessage);
    }

    public void sendCanceledMessage(long userId) {
        EditMessageText message = EditMessageText.builder().chatId(userId)
                .text("Встреча была отменена!").build();

        executeMessage(message);
    }

    public void sendMessageSentSuccessfully(long userId) {
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("create.meeting.sent.participants"), null);
        executeMessage(editMessage);
    }


    public void sendAwaitingMessage(long userId, Meeting meeting) {
        InlineKeyboardButton sendButton = InlineKeyboardButton.builder().callbackData(ToggleButton.SEND.name())
                .text(Emojis.MESSAGE.getEmojiSpace() + "Отправить запрос участникам").build();

        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getEditingInlineButtons();
        keyboard.add(0, List.of(sendButton));

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard).build();

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);

        Account account = accountService.getByUserId(userId).orElseThrow();
        String zoneId = account.getZoneId();
        List<ZonedDateTime> dates = meeting.getDatesWithZoneId(zoneId);
        String timesText = messageUtils.generateDatesText(dates);

        String textMeeting =
                localeMessageService.getMessage("create.meeting.awaiting",
                        meetingMessage.participants(), meetingMessage.subject(), meetingMessage.questions(),
                        meetingMessage.duration(), timesText, meetingMessage.address());

        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, textMeeting,
                keyboardMarkup);

        executeMessage(editMessage);
    }

    public void sendMeetingToParticipants(Meeting meeting) {
        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);

        List<Account> participants = accountService.getAccountByMeetingId(meeting.getId());

        InlineKeyboardMarkup keyboard =
                meetingKeyboard.getMeetingConfirmKeyboard(meeting);

        for (Account account : participants) {

            if (Objects.equals(account.getId(), meeting.getOwner().getId())) continue;

            String zoneId = account.getZoneId();
            List<ZonedDateTime> dates = meeting.getDatesWithZoneId(zoneId);
            String datesText = messageUtils.generateDatesText(dates);

            String textMeeting = localeMessageService.getMessage("create.meeting.awaiting.participants",
                    meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                    meetingMessage.participants(), meetingMessage.duration(), datesText,
                    meetingMessage.address());

            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(account.getId(), textMeeting, keyboard);

            executeSendMessage(sendMessage);
        }
    }

}
