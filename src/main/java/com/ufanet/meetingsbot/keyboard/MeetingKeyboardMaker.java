package com.ufanet.meetingsbot.keyboard;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.EditState;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.utils.Emojis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.ufanet.meetingsbot.utils.CustomFormatter.DATE_WEEK_FORMATTER;


@Component
@RequiredArgsConstructor
public class MeetingKeyboardMaker extends KeyboardMaker {

    public List<List<InlineKeyboardButton>> getSubjectDurationInlineMarkup(Meeting meeting) {
        Subject subject = meeting.getSubject();
        Integer duration = subject.getDuration();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 15; i <= 90; i += 15) {
            InlineKeyboardButton button;
            if (duration != null && duration == i) {
                button = defaultInlineButton(Emojis.GREEN_SELECTED.getEmoji() + i, " ");
            } else {
                button = defaultInlineButton(Integer.toString(i), Integer.toString(i));
            }
            row.add(button);
        }
        keyboard.add(row);
        return keyboard;
    }

    public List<InlineKeyboardButton> defaultRowHelperInlineButtons(boolean next) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(cancel);
        if (next) {
            buttons.add(stepNext);
        }
        return buttons;
    }

    public List<List<InlineKeyboardButton>> getParticipantsInlineButtons(Set<Account> groupMembers,
                                                                         Set<Account> meetingMembers) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (Account member : groupMembers) {
            List<InlineKeyboardButton> button;
            if (meetingMembers.contains(member)) {
                button = List.of(defaultInlineButton(
                        Emojis.GREEN_SELECTED.getEmoji() + " " + member.getFirstname(),
                        String.valueOf(member.getId())));
            } else {
                button = List.of(defaultInlineButton(member.getFirstname(),
                        Long.toString(member.getId())));
            }

            keyboard.add(button);
        }
        return keyboard;
    }

    public InlineKeyboardMarkup getGroupsInlineMarkup(Meeting meeting, List<Group> groups) {
        Group currentGroup = meeting.getGroup();
        boolean hasGroup = currentGroup != null && currentGroup.getId() != null;
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Group group : groups) {
            List<InlineKeyboardButton> button;
            if (hasGroup && Objects.equals(group.getId(), currentGroup.getId())) {
                button = List.of(defaultInlineButton(Emojis.GREEN_SELECTED.getEmoji() +
                        group.getTitle(), " "));
            } else {
                button = List.of(defaultInlineButton(group.getTitle(),
                        String.valueOf(group.getId())));
            }
            keyboard.add(button);

        }
        keyboard.add(defaultRowHelperInlineButtons(hasGroup));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public List<List<InlineKeyboardButton>> getQuestionsInlineMarkup(Meeting meeting) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        Subject subject = meeting.getSubject();
        Set<Question> questions = subject.getQuestions();
        for (Question question : questions) {
            InlineKeyboardButton questionButton = InlineKeyboardButton.builder()
                    .text(Emojis.GREEN_SELECTED.getEmojiSpace()
                            + question.getTitle()).callbackData(question.getTitle()).build();
            keyboard.add(List.of(questionButton));
        }
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> getEditingInlineButtons() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton subject = InlineKeyboardButton.builder()
                .text(Emojis.CLIPBOARD.getEmojiSpace() + "Ред. темы")
                .callbackData(EditState.EDIT_SUBJECT.name()).build();

        InlineKeyboardButton participants = InlineKeyboardButton.builder()
                .text(Emojis.PARTICIPANTS.getEmojiSpace() + "Изм. участников")
                .callbackData(EditState.EDIT_PARTICIPANT.name()).build();

        keyboard.add(List.of(subject, participants));

        InlineKeyboardButton time = InlineKeyboardButton.builder()
                .text(Emojis.CLOCK.getEmojiSpace() + "Изм. время")
                .callbackData(EditState.EDIT_TIME.name()).build();
        InlineKeyboardButton address = InlineKeyboardButton.builder()
                .text(Emojis.OFFICE.getEmojiSpace() + "Изм. адрес")
                .callbackData(EditState.EDIT_ADDRESS.name()).build();

        keyboard.add(List.of(time, address));
        keyboard.add(List.of(cancel));
        return keyboard;
    }

    public InlineKeyboardMarkup getMeetingConfirmKeyboard(Meeting meeting) {
        Long meetingId = meeting.getId();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton change = InlineKeyboardButton.builder()
                .text(Emojis.CHANGE.getEmojiSpace() + "Изменить указанные интервалы")
                .callbackData(UpcomingState.UPCOMING_EDIT_MEETING_TIME.name() + " " + meetingId).build();
        InlineKeyboardButton confirm = InlineKeyboardButton.builder()
                .text(Emojis.GREEN_SELECTED.getEmojiSpace() + "Подтверждаю")
                .callbackData(UpcomingState.UPCOMING_CONFIRM_MEETING_TIME.name() + " " + meetingId).build();
        InlineKeyboardButton cancel = InlineKeyboardButton.builder()
                .text(Emojis.CANCEL_CIRCLE.getEmojiSpace() + "Не смогу прийти")
                .callbackData(UpcomingState.UPCOMING_CANCEL_MEETING_TIME.name() + " " + meetingId).build();
        keyboard.add(List.of(change));
        keyboard.add(List.of(cancel, confirm));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    //TODO поменять подход
    public InlineKeyboardMarkup getChangeMeetingTimeKeyboard(long meetingId, List<AccountTime> accountTimes) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        Map<LocalDate, List<AccountTime>> collected = new TreeMap<>();

        accountTimes.forEach(at -> {
            MeetingTime meetingTime = at.getMeetingTime();
            MeetingDate meetingDate = meetingTime.getMeetingDate();
            LocalDate localDate = meetingDate.getDate();
            List<AccountTime> times = collected.getOrDefault(localDate, new ArrayList<>());
            times.add(at);
            collected.put(localDate, times);
        });
        //TODO красивый вывод
        for (Map.Entry<LocalDate, List<AccountTime>> entry : collected.entrySet()) {
            LocalDate dateTime = entry.getKey();
            InlineKeyboardButton questionButton = InlineKeyboardButton.builder()
                    .text(Emojis.CALENDAR.getEmojiSpace() + dateTime.format(DATE_WEEK_FORMATTER))
                    .callbackData(" ").build();
            keyboard.add(List.of(questionButton));

            List<AccountTime> times = entry.getValue();
            List<InlineKeyboardButton> buttons = new ArrayList<>();

            for (AccountTime accountTime : times) {
                LocalDateTime localDateTime = accountTime.getMeetingTime().getTime();
                InlineKeyboardButton time = InlineKeyboardButton.builder()
                        .text(Emojis.GREEN_SELECTED.getEmojiSpace() + localDateTime.toLocalTime().toString())
                        .callbackData(UpcomingState.UPCOMING_EDIT_MEETING_TIME.name() + " " + meetingId + " " + accountTime.getId()).build();

                if (accountTime.getStatus().equals(Status.CANCELED)) {
                    time.setText(localDateTime.toLocalTime().toString());
                }
                buttons.add(time);
            }
            keyboard.add(buttons);
        }

        InlineKeyboardButton confirm = InlineKeyboardButton.builder()
                .text(Emojis.GREEN_SELECTED.getEmojiSpace() + "Подтверждаю")
                .callbackData(UpcomingState.UPCOMING_CONFIRM_MEETING_TIME.name() + " " + meetingId).build();
        InlineKeyboardButton cancel = InlineKeyboardButton.builder()
                .text(Emojis.CANCEL_CIRCLE.getEmojiSpace() + "Не смогу прийти")
                .callbackData(UpcomingState.UPCOMING_CANCEL_MEETING_TIME.name() + " " + meetingId).build();
        keyboard.add(List.of(cancel, confirm));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }


    public InlineKeyboardMarkup getMeetingUpcomingMarkup(long userId, Meeting meeting,
                                                         Optional<AccountTime> accountTime) {
        Long meetingId = meeting.getId();
        InlineKeyboardButton btn1 = defaultInlineButton("Я опаздываю", UpcomingState.UPCOMING_IAMLATE + " " + meetingId);
        InlineKeyboardButton btn2 = defaultInlineButton("Я готов", UpcomingState.UPCOMING_IAMREADY + " " + meetingId);
        InlineKeyboardButton btn3 = defaultInlineButton("Я не приду", UpcomingState.UPCOMING_IWILLNOTCOME + " " + meetingId);
        InlineKeyboardButton btn4 = defaultInlineButton("Пингануть участника", "Пингануть участника");
        InlineKeyboardButton btn5 = defaultInlineButton("Назад", UpcomingState.UPCOMING_MEETINGS.name());
        InlineKeyboardButton btn6 = defaultInlineButton("Отменить встречу",
                UpcomingState.UPCOMING_CANCEL_BY_OWNER.name() + " " + meeting.getId());

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        boolean isOwner = meeting.getOwner().getId() == userId;
        if (isOwner) {
            keyboard.add(List.of(btn4));
            keyboard.add(List.of(btn6, btn5));
        } else if (accountTime.isPresent()) {
            Status status = accountTime.get().getStatus();
            switch (status) {
                case AWAITING -> {
                    btn1.setText(Emojis.GREEN_SELECTED.getEmojiSpace() + "Я опаздываю");
                    btn1.setCallbackData(" ");
                }
                case CONFIRMED -> {
                    btn2.setText(Emojis.GREEN_SELECTED.getEmojiSpace() + "Я готов");
                    btn2.setCallbackData(" ");
                }
                case CANCELED -> {
                    btn3.setText(Emojis.GREEN_SELECTED.getEmojiSpace() + "Я не приду");
                    btn3.setCallbackData(" ");
                }
            }
            keyboard.add(List.of(btn1, btn2));
            keyboard.add(List.of(btn5, btn3));
        }
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }
}
