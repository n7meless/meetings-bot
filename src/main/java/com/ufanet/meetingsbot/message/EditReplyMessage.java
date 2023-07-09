package com.ufanet.meetingsbot.message;

import com.ufanet.meetingsbot.constants.Emojis;
import com.ufanet.meetingsbot.constants.state.EditState;
import com.ufanet.meetingsbot.constants.type.EventType;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.message.keyboard.CalendarKeyboardMaker;
import com.ufanet.meetingsbot.message.keyboard.MeetingKeyboardMaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EditReplyMessage extends ReplyMessage {
    private final MeetingKeyboardMaker meetingKeyboard;
    private final CalendarKeyboardMaker calendarKeyboard;

    private String getMainText(MeetingDto meetingDto) {
        AccountDto owner = meetingDto.getOwner();
        String zoneId = owner.getZoneId();
        List<ZonedDateTime> dates = meetingDto.getDatesWithZoneId(zoneId);
        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meetingDto);
        String times = messageUtils.generateDatesText(dates);
        return localeMessageService.getMessage("create.meeting.awaiting",
                meetingMessage.participants(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.duration(), times, meetingMessage.address());
    }

    public void sendEditAddress(long userId, MeetingDto meetingDto) {
        String firstText = getMainText(meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.INFO.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.address"));

        InlineKeyboardButton readyButton = meetingKeyboard.getReadyInlineButton(EventType.CREATE.name());

        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                firstText + secondText,
                meetingKeyboard.buildInlineMarkup(List.of(List.of(readyButton))));

        executeEditOrSendMessage(message);
    }

    public void sendEditTime(long userId, MeetingDto meetingDto) {
        AccountDto owner = meetingDto.getOwner();
        String zoneId = owner.getZoneId();
        String firstText = getMainText(meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.INFO.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.time"));

        List<List<InlineKeyboardButton>> keyboard = calendarKeyboard.getTimeInlineMarkup(meetingDto, zoneId);
        keyboard.add(List.of(meetingKeyboard.getReadyInlineButton(EventType.CREATE.name())));
        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                firstText + secondText,
                meetingKeyboard.buildInlineMarkup(keyboard));
        executeEditOrSendMessage(message);
    }

    public void sendEditParticipants(long userId, MeetingDto meetingDto, Set<AccountDto> members) {
        String firstText = getMainText(meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.INFO.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.participants"));

        Set<AccountDto> participants = meetingDto.getParticipants();
        List<List<InlineKeyboardButton>> keyboard =
                meetingKeyboard.getParticipantsInlineButtons(members, participants);
        if (participants.size() > 1) {
            keyboard.add(List.of(meetingKeyboard.getReadyInlineButton(EventType.CREATE.name())));
        }
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, firstText + secondText,
                meetingKeyboard.buildInlineMarkup(keyboard));
        executeEditOrSendMessage(editMessage);
    }

    public void semdEditSubject(long userId, MeetingDto meetingDto) {
        String firstText = getMainText(meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.INFO.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.subject"));

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(meetingKeyboard.defaultInlineButton(Emojis.DURATION.getEmojiSpace() +
                "Выбрать продолжительность", EditState.EDIT_SUBJECT_DURATION.name())));
        keyboard.add(List.of(meetingKeyboard.getReadyInlineButton(EventType.CREATE.name())));
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, firstText + secondText,
                meetingKeyboard.buildInlineMarkup(keyboard));
        executeEditOrSendMessage(editMessage);
    }

    public void sendEditSubjectDuration(long userId, MeetingDto meetingDto) {
        String firstText = getMainText(meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.INFO.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.duration"));

        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getSubjectDurationInlineMarkup(meetingDto);
        InlineKeyboardButton btn1 =
                meetingKeyboard.defaultInlineButton(Emojis.QUESTION.getEmojiSpace() + "Выбрать вопросы",
                        EditState.EDIT_QUESTION.name());
        InlineKeyboardButton btn2 = meetingKeyboard.getReadyInlineButton(EventType.CREATE.name());

        keyboard.add(List.of(btn1));
        keyboard.add(List.of(btn2));

        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, firstText + secondText,
                meetingKeyboard.buildInlineMarkup(keyboard));
        executeEditOrSendMessage(editMessage);
    }

    public void sendEditQuestion(long userId, MeetingDto meetingDto) {
        String firstText = getMainText(meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.INFO.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.questions"));

        Set<String> questions = meetingDto.getSubjectDto().getQuestions();
        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getQuestionsInlineMarkup(questions);
        if (questions.size() > 0) {
            keyboard.add(List.of(meetingKeyboard.getReadyInlineButton(EventType.CREATE.name())));
        }
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, firstText + secondText,
                meetingKeyboard.buildInlineMarkup(keyboard));
        executeEditOrSendMessage(editMessage);
    }
}
