package com.ufanet.meetingsbot.message;

import com.ufanet.meetingsbot.constants.Emojis;
import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.entity.Group;
import com.ufanet.meetingsbot.message.keyboard.CalendarKeyboardMaker;
import com.ufanet.meetingsbot.message.keyboard.MeetingKeyboardMaker;
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

@Service
@RequiredArgsConstructor
public class MeetingReplyMessage extends ReplyMessage {
    private final MeetingKeyboardMaker meetingKeyboard;
    private final CalendarKeyboardMaker calendarKeyboard;

    public void sendGroupMessage(long userId, List<Group> groups) {
        if (groups.isEmpty()) {
            SendMessage sendMessage =
                    messageUtils.generateSendMessage(userId,
                            localeMessageService.getMessage("create.meeting.group.notexists"));
            executeSendMessage(sendMessage);
        } else {
            EditMessageText editMessage =
                    messageUtils.generateEditMessageHtml(userId,
                            localeMessageService.getMessage("create.meeting.group"),
                            meetingKeyboard.getGroupsInlineMarkup(groups));

            executeEditOrSendMessage(editMessage);
        }
    }

    public void sendParticipantsMessage(long userId, MeetingDto meetingDto, Set<AccountDto> members) {

        Set<AccountDto> participantIds = meetingDto.getParticipants();

        List<List<InlineKeyboardButton>> keyboard =
                meetingKeyboard.getParticipantsInlineButtons(members, participantIds);
        List<InlineKeyboardButton> helperRowButtons =
                meetingKeyboard.defaultToggleInlineButtons(participantIds.size() > 1);
        keyboard.add(helperRowButtons);

        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("create.meeting.participants"),
                meetingKeyboard.buildInlineMarkup(keyboard));

        executeEditOrSendMessage(message);
    }

    public void sendSubjectMessage(long userId) {
        List<InlineKeyboardButton> rowHelperButtons =
                meetingKeyboard.defaultToggleInlineButtons(false);
        InlineKeyboardMarkup keyboardMarkup =
                meetingKeyboard.buildInlineMarkup(List.of(rowHelperButtons));
        EditMessageText editMessage =
                messageUtils.generateEditMessageHtml(userId,
                        localeMessageService.getMessage("create.meeting.subject"),
                        keyboardMarkup);
        executeEditOrSendMessage(editMessage);
    }

    public void sendQuestionMessage(long userId, MeetingDto meetingDto) {
        Set<String> questions = meetingDto.getSubjectDto().getQuestions();
        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getQuestionsInlineMarkup(questions);
        keyboard.add(meetingKeyboard.defaultToggleInlineButtons(questions.size() > 0));
        EditMessageText editMessage =
                messageUtils.generateEditMessageHtml(userId,
                        localeMessageService.getMessage("create.meeting.question"),
                        meetingKeyboard.buildInlineMarkup(keyboard));
        executeEditOrSendMessage(editMessage);
    }

    public void sendDateMessage(long userId, MeetingDto meetingDto, String callback) {
        String zoneId = meetingDto.getOwner().getZoneId();
        List<List<InlineKeyboardButton>> keyboard =
                calendarKeyboard.getCalendarInlineMarkup(meetingDto, callback, zoneId);
        keyboard.add(meetingKeyboard.defaultToggleInlineButtons(meetingDto.getDates().size() > 0));
        EditMessageText message =
                messageUtils.generateEditMessageHtml(userId,
                        localeMessageService.getMessage("create.meeting.date"),
                        meetingKeyboard.buildInlineMarkup(keyboard));
        executeEditOrSendMessage(message);
    }

    public void sendTimeMessage(long userId, MeetingDto meetingDto) {
        String zoneId = meetingDto.getOwner().getZoneId();
        List<List<InlineKeyboardButton>> keyboard = calendarKeyboard.getTimeInlineMarkup(meetingDto, zoneId);
        boolean hasTime = meetingDto.getDates().stream().anyMatch(t -> t.getMeetingTimes().size() > 0);
        keyboard.add(meetingKeyboard.defaultToggleInlineButtons(hasTime));

        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("create.meeting.time"),
                meetingKeyboard.buildInlineMarkup(keyboard));

        executeEditOrSendMessage(message);
    }

    public void sendAddressMessage(long userId) {
        List<InlineKeyboardButton> buttons =
                meetingKeyboard.defaultToggleInlineButtons(true);

        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("create.meeting.address"),
                meetingKeyboard.buildInlineMarkup(List.of(buttons)));

        executeEditOrSendMessage(message);
    }

    public void sendSubjectDurationMessage(long userId, MeetingDto meetingDto) {
        List<List<InlineKeyboardButton>> keyboard =
                meetingKeyboard.getSubjectDurationInlineMarkup(meetingDto);

        List<InlineKeyboardButton> toggleButtons =
                meetingKeyboard.defaultToggleInlineButtons(meetingDto.getSubjectDto().getDuration() != null);
        keyboard.add(toggleButtons);

        EditMessageText editMessage =
                messageUtils.generateEditMessageHtml(userId,
                        localeMessageService.getMessage("create.meeting.duration"),
                        meetingKeyboard.buildInlineMarkup(keyboard));
        executeEditOrSendMessage(editMessage);
    }

    public void sendCanceledMessage(long userId) {
        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("create.meeting.canceled"), null);

        executeEditOrSendMessage(message);
    }

    public void sendMessageSentSuccessfully(long userId) {
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("create.meeting.sent.participants"), null);
        executeEditOrSendMessage(editMessage);
    }


    public void sendAwaitingMeetingMessage(long userId, MeetingDto meetingDto) {
        InlineKeyboardButton sendButton =
                meetingKeyboard.defaultInlineButton(Emojis.MESSAGE.getEmojiSpace() +
                        "Отправить запрос участникам", ToggleButton.SEND.name());

        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getEditingInlineButtons();
        keyboard.add(0, List.of(sendButton));

        InlineKeyboardMarkup keyboardMarkup = meetingKeyboard.buildInlineMarkup(keyboard);

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meetingDto);

        AccountDto owner = meetingDto.getOwner();
        String zoneId = owner.getZoneId();

        List<ZonedDateTime> dates = meetingDto.getDatesWithZoneId(zoneId);
        String timesText = messageUtils.generateDatesText(dates);

        String textMeeting =
                localeMessageService.getMessage("create.meeting.awaiting",
                        meetingMessage.participants(), meetingMessage.subject(), meetingMessage.questions(),
                        meetingMessage.duration(), timesText, meetingMessage.address());

        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, textMeeting,
                keyboardMarkup);

        executeEditOrSendMessage(editMessage);
    }

    public void sendMeetingToParticipants(MeetingDto meetingDto) {
        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meetingDto);

        Set<AccountDto> accountDtos = meetingDto.getParticipants();

        InlineKeyboardMarkup keyboard =
                meetingKeyboard.getMeetingConfirmKeyboard(meetingDto.getId());

        for (AccountDto account : accountDtos) {

            if (Objects.equals(account.getId(), meetingDto.getOwner().getId())) continue;

            String zoneId = account.getZoneId();
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
