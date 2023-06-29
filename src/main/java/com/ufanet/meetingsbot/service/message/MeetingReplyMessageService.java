package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.keyboard.CalendarKeyboardMaker;
import com.ufanet.meetingsbot.keyboard.MeetingKeyboardMaker;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.GroupService;
import com.ufanet.meetingsbot.utils.Emojis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingReplyMessageService extends ReplyMessageService {
    private final MeetingKeyboardMaker meetingKeyboard;
    private final CalendarKeyboardMaker calendarKeyboard;
    private final AccountService accountService;
    private final GroupService groupService;

    public void sendGroupMessage(long userId, MeetingDto meeting) {
        List<Group> groups = groupService.getGroupsByMemberId(userId);

        EditMessageText editMessage =
                messageUtils.generateEditMessageHtml(userId, localeMessageService.getMessage("create.meeting.group"),
                        meetingKeyboard.getGroupsInlineMarkup(meeting, groups));

        executeMessage(editMessage);
    }

    public void sendParticipantsMessage(long userId, MeetingDto meetingDto) {
        Set<AccountDto> members =
                accountService.getAccountsByGroupsIdAndIdNot(meetingDto.getGroupId(), userId).stream()
                        .map(accountService::mapToDto).collect(Collectors.toSet());

        Set<AccountDto> participantIds = meetingDto.getParticipants();

        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getParticipantsInlineButtons(members, participantIds);
        keyboard.add(meetingKeyboard.defaultRowHelperInlineButtons(participantIds.size() > 1));

        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("create.meeting.participants"),
                meetingKeyboard.buildInlineMarkup(keyboard));

        executeMessage(message);
    }

    public void sendSubjectMessage(long userId, MeetingDto meetingDto) {
        InlineKeyboardMarkup keyboardMarkup;
        keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(meetingKeyboard.defaultRowHelperInlineButtons(false))
                .build();

        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, "Укажите тему встречи",
                keyboardMarkup);
        executeMessage(editMessage);
    }

    public void sendQuestionMessage(long userId, MeetingDto meetingDto) {
        Set<String> questions = meetingDto.getSubjectDto().getQuestions();
        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getQuestionsInlineMarkup(questions);
        keyboard.add(meetingKeyboard.defaultRowHelperInlineButtons(questions.size() > 0));
        EditMessageText editMessage =
                messageUtils.generateEditMessageHtml(userId, localeMessageService.getMessage("create.meeting.question"),
                        meetingKeyboard.buildInlineMarkup(keyboard));
        executeMessage(editMessage);
    }

    public void sendDateMessage(long userId, MeetingDto meetingDto, String callback) {
        Account account = accountService.getByUserId(userId).orElseThrow();
        String zoneId = account.getZoneId();
        List<List<InlineKeyboardButton>> keyboard = calendarKeyboard.getCalendarInlineMarkup(meetingDto, callback, zoneId);
        keyboard.add(meetingKeyboard.defaultRowHelperInlineButtons(meetingDto.getDates().size() > 0));
        EditMessageText message =
                messageUtils.generateEditMessageHtml(userId, localeMessageService.getMessage("create.meeting.date"),
                        meetingKeyboard.buildInlineMarkup(keyboard));

        executeMessage(message);
    }

    public void sendTimeMessage(long userId, MeetingDto meetingDto) {
        Account account = accountService.getByUserId(userId).orElseThrow();
        String zoneId = account.getZoneId();
        List<List<InlineKeyboardButton>> keyboard = calendarKeyboard.getTimeInlineMarkup(meetingDto, zoneId);
        boolean hasTime = meetingDto.getDates().stream().anyMatch(t -> t.getMeetingTimes().size() > 0);
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

    public void sendSubjectDurationMessage(long userId, MeetingDto meetingDto) {
        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getSubjectDurationInlineMarkup(meetingDto);

        keyboard.add(meetingKeyboard.defaultRowHelperInlineButtons(meetingDto.getSubjectDto().getDuration() != null));

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


    public void sendAwaitingMessage(long userId, MeetingDto meetingDto) {
        InlineKeyboardButton sendButton = InlineKeyboardButton.builder().callbackData(ToggleButton.SEND.name())
                .text(Emojis.MESSAGE.getEmojiSpace() + "Отправить запрос участникам").build();

        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getEditingInlineButtons();
        keyboard.add(0, List.of(sendButton));

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard).build();

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meetingDto);

        Account account = accountService.getByUserId(userId).orElseThrow();
        String zoneId = account.getZoneId();
        List<ZonedDateTime> dates = meetingDto.getDatesWithZoneId(zoneId);
        String timesText = messageUtils.generateDatesText(dates);

        String textMeeting =
                localeMessageService.getMessage("create.meeting.awaiting",
                        meetingMessage.participants(), meetingMessage.subject(), meetingMessage.questions(),
                        meetingMessage.duration(), timesText, meetingMessage.address());

        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, textMeeting,
                keyboardMarkup);

        executeMessage(editMessage);
    }

    public void sendMeetingToParticipants(MeetingDto meetingDto) {
        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meetingDto);

//        List<Account> participants = accountService.getAccountByMeetingId(meetingDto.getId());
        Set<AccountDto> accountDtos = meetingDto.getParticipants();

        InlineKeyboardMarkup keyboard =
                meetingKeyboard.getMeetingConfirmKeyboard(meetingDto.getId());

        for (AccountDto account : accountDtos) {

            if (Objects.equals(account.getId(), meetingDto.getOwner().getId())) continue;

            String zoneId = account.getTimeZone();
            List<ZonedDateTime> dates = meetingDto.getDatesWithZoneId(zoneId);
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
