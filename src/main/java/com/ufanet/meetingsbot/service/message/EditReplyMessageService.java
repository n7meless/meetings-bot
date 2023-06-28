package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.constants.state.EditState;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.keyboard.CalendarKeyboardMaker;
import com.ufanet.meetingsbot.keyboard.MeetingKeyboardMaker;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.utils.Emojis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EditReplyMessageService extends ReplyMessageService {
    private final MeetingKeyboardMaker meetingKeyboard;
    private final CalendarKeyboardMaker calendarKeyboard;
    private final AccountService accountService;

    private String getMainText(long userId, MeetingDto meetingDto) {
        Account account = accountService.getByUserId(userId).orElseThrow();
        String zoneId = account.getZoneId();
        List<ZonedDateTime> dates = meetingDto.getDatesWithZoneId(zoneId);
        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meetingDto);
        String times = messageUtils.generateDatesText(dates);
        return localeMessageService.getMessage("create.meeting.awaiting",
                meetingMessage.participants(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.duration(), times, meetingMessage.address());
    }

    public void editAddress(long userId, MeetingDto meetingDto) {
        String firstText = getMainText(userId, meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.BANGBANG.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.address"));

        InlineKeyboardButton readyButton = meetingKeyboard.getReadyInlineButton(AccountState.CREATE.name());

        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                firstText + secondText,
                meetingKeyboard.buildInlineMarkup(List.of(List.of(readyButton))));

        executeMessage(message);
    }

    public void editTime(long userId, MeetingDto meetingDto) {
        Account account = accountService.getByUserId(userId).orElseThrow();
        String zoneId = account.getZoneId();
        String firstText = getMainText(userId, meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.BANGBANG.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.time"));

        List<List<InlineKeyboardButton>> keyboard = calendarKeyboard.getTimeInlineMarkup(meetingDto, zoneId);
        keyboard.add(List.of(meetingKeyboard.getReadyInlineButton(AccountState.CREATE.name())));
        EditMessageText message = messageUtils.generateEditMessageHtml(userId,
                firstText + secondText,
                meetingKeyboard.buildInlineMarkup(keyboard));
        executeMessage(message);
    }

    public void editParticipants(long userId, MeetingDto meetingDto) {
        String firstText = getMainText(userId, meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.BANGBANG.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.participants"));

        Set<AccountDto> members = accountService.getAccountByGroupsIdAndIdNot(meetingDto.getGroupId(), userId)
                .stream().map(accountService::mapToDto).collect(Collectors.toSet());

        Set<AccountDto> participants = meetingDto.getParticipants();
        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getParticipantsInlineButtons(members, participants);
        if (participants.size() > 0) {
            keyboard.add(List.of(meetingKeyboard.getReadyInlineButton(AccountState.CREATE.name())));
        }
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, firstText + secondText,
                meetingKeyboard.buildInlineMarkup(keyboard));
        executeMessage(editMessage);
    }

    public void editSubject(long userId, MeetingDto meetingDto) {
        String firstText = getMainText(userId, meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.BANGBANG.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.subject"));

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(List.of(meetingKeyboard.defaultInlineButton(Emojis.DURATION.getEmojiSpace() +
                "Выбрать продолжительность", EditState.EDIT_SUBJECT_DURATION.name())));
        keyboard.add(List.of(meetingKeyboard.getReadyInlineButton(AccountState.CREATE.name())));
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, firstText + secondText,
                meetingKeyboard.buildInlineMarkup(keyboard));
        executeMessage(editMessage);
    }

    public void editSubjectDuration(long userId, MeetingDto meetingDto) {
        String firstText = getMainText(userId, meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.BANGBANG.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.duration"));

        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getSubjectDurationInlineMarkup(meetingDto);
        keyboard.add(List.of(meetingKeyboard.defaultInlineButton(Emojis.QUESTION.getEmojiSpace() + "Выбрать вопросы",
                EditState.EDIT_QUESTION.name())));
        keyboard.add(List.of(meetingKeyboard.getReadyInlineButton(AccountState.CREATE.name())));

        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, firstText + secondText,
                meetingKeyboard.buildInlineMarkup(keyboard));
        executeMessage(editMessage);
    }

    public void editQuestion(long userId, MeetingDto meetingDto) {
        String firstText = getMainText(userId, meetingDto);
        String secondText = messageUtils.buildText("\n\n", Emojis.BANGBANG.getEmojiSpace(),
                localeMessageService.getMessage("edit.meeting.questions"));

        Set<String> questions = meetingDto.getQuestions();
        List<List<InlineKeyboardButton>> keyboard = meetingKeyboard.getQuestionsInlineMarkup(questions);
        if (questions.size() > 0) {
            keyboard.add(List.of(meetingKeyboard.getReadyInlineButton(AccountState.CREATE.name())));
        }
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, firstText + secondText,
                meetingKeyboard.buildInlineMarkup(keyboard));
        executeMessage(editMessage);
    }
}
