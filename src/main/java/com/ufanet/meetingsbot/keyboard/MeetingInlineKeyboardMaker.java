package com.ufanet.meetingsbot.keyboard;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.ToggleButton;
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
public class MeetingInlineKeyboardMaker {
    private final CalendarKeyboardMaker calendarKeyboardMaker;
    private final InlineKeyboardButton stepNext = defaultInlineButton("Далее", ToggleButton.NEXT.name());
    private final InlineKeyboardButton cancel = defaultInlineButton("Отменить", ToggleButton.CANCEL.name());

    public InlineKeyboardMarkup getCalendarInlineMarkup(Meeting meeting, String callback) {
        List<List<InlineKeyboardButton>> keyboard = calendarKeyboardMaker.getCalendarInlineMarkup(meeting, callback);
        boolean next = meeting.getDates().size() > 0;
        List<InlineKeyboardButton> defaultRowHelperInlineMarkup = defaultRowHelperInlineButtons(next, false);
        keyboard.add(defaultRowHelperInlineMarkup);
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardMarkup getTimeInlineMarkup(Meeting meeting) {
        List<List<InlineKeyboardButton>> keyboard = calendarKeyboardMaker.getTimeInlineMarkup(meeting);
        boolean hasTime = meeting.getDates().stream().anyMatch(t -> t.getMeetingTimes().size() > 0);
        keyboard.add(defaultRowHelperInlineButtons(hasTime, false));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardMarkup getSubjectDurationInlineMarkup(Meeting meeting) {
        Subject subject = meeting.getSubject();
        Integer duration = subject.getDuration();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 15; i <= 90; i += 15) {
            InlineKeyboardButton button;
            if (duration != null && duration == i) {
                button = defaultInlineButton(Emojis.SELECTED.getEmoji() + i, " ");
            } else {
                button = defaultInlineButton(Integer.toString(i), Integer.toString(i));
            }
            row.add(button);
        }
        keyboard.add(row);
        keyboard.add(defaultRowHelperInlineButtons(duration != null, false));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardButton defaultInlineButton(String text, String callback) {
        return InlineKeyboardButton.builder().text(text)
                .callbackData(callback).build();
    }

    public List<InlineKeyboardButton> defaultRowHelperInlineButtons(boolean next, boolean ready) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (next) {
            buttons.add(cancel);
            buttons.add(stepNext);
        } else {
            buttons.add(cancel);
        }
        return buttons;
    }

    public InlineKeyboardMarkup getParticipantsInlineMarkup(Set<Account> participants,
                                                            Set<Account> selectedMembers) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (Account participant : participants) {
            List<InlineKeyboardButton> button;
            if (selectedMembers.contains(participant)) {
                button = List.of(defaultInlineButton(
                        Emojis.SELECTED.getEmoji() + " " + participant.getFirstname(),
                        String.valueOf(participant.getId())));
            } else {
                button = List.of(defaultInlineButton(participant.getFirstname(),
                        Long.toString(participant.getId())));
            }

            keyboard.add(button);
        }
        boolean next = selectedMembers.size() > 0;
        keyboard.add(defaultRowHelperInlineButtons(next, false));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardMarkup getGroupsInlineMarkup(Meeting meeting, List<Group> groups) {
        Group currentGroup = meeting.getGroup();
        boolean hasGroup = currentGroup != null && currentGroup.getId() != null;
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (Group group : groups) {
            List<InlineKeyboardButton> button;
            if (hasGroup && Objects.equals(group.getId(), currentGroup.getId())) {
                button = List.of(defaultInlineButton(Emojis.SELECTED.getEmoji() + group.getTitle(), " "));
            } else {
                button = List.of(defaultInlineButton(group.getTitle(),
                        String.valueOf(group.getId())));
            }
            keyboard.add(button);

        }
        keyboard.add(defaultRowHelperInlineButtons(hasGroup, false));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardMarkup getQuestionsInlineMarkup(Meeting meeting) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        Subject subject = meeting.getSubject();
        Set<Question> questions = subject.getQuestions();
        for (Question question : questions) {
            InlineKeyboardButton questionButton = InlineKeyboardButton.builder().text(Emojis.SELECTED.getEmojiSpace()
                    + question.getTitle()).callbackData(question.getTitle()).build();
            keyboard.add(List.of(questionButton));
        }
        boolean next = questions.size() > 0;
        keyboard.add(defaultRowHelperInlineButtons(next, false));
        return InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }

    public List<List<InlineKeyboardButton>> getEditingInlineButtons() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

//        InlineKeyboardButton subject = InlineKeyboardButton.builder().text("Ред. темы").callbackData("Ред. темы").build();
//        InlineKeyboardButton participants = InlineKeyboardButton.builder().text("Изм. участников").callbackData("Изм. участников").build();
//
//        keyboard.add(List.of(subject, participants));
//
//        InlineKeyboardButton time = InlineKeyboardButton.builder().text("Изм. время").callbackData("Изм. время").build();
//        InlineKeyboardButton address = InlineKeyboardButton.builder().text("Изм. адрес").callbackData("Изм. адрес").build();
        InlineKeyboardButton change = InlineKeyboardButton.builder().text("Изменить").callbackData("Изменить").build();

        keyboard.add(List.of(change));
        return keyboard;
    }

    public InlineKeyboardMarkup getMeetingConfirmKeyboard(Meeting meeting) {
        Long meetingId = meeting.getId();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        InlineKeyboardButton change = InlineKeyboardButton.builder()
                .text(Emojis.CHANGE.getEmojiSpace() + "Изменить указанные интервалы")
                .callbackData(UpcomingState.UPCOMING_EDIT_MEETING_TIME.name() + " " + meetingId).build();
        InlineKeyboardButton confirm = InlineKeyboardButton.builder()
                .text(Emojis.SELECTED.getEmojiSpace() + "Подтверждаю")
                .callbackData(UpcomingState.UPCOMING_CONFIRM_MEETING_TIME.name() + " " + meetingId).build();
        InlineKeyboardButton cancel = InlineKeyboardButton.builder()
                .text(Emojis.CANCEL.getEmojiSpace() + "Не смогу прийти")
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
                        .text(Emojis.SELECTED.getEmojiSpace() + localDateTime.toLocalTime().toString())
                        .callbackData(UpcomingState.UPCOMING_EDIT_MEETING_TIME.name() + " " + meetingId + " " + accountTime.getId()).build();

                if (accountTime.getStatus().equals(Status.CANCELED)) {
                    time.setText(localDateTime.toLocalTime().toString());
                }
                buttons.add(time);
            }
            keyboard.add(buttons);
        }

        InlineKeyboardButton confirm = InlineKeyboardButton.builder()
                .text(Emojis.SELECTED.getEmojiSpace() + "Подтверждаю")
                .callbackData(UpcomingState.UPCOMING_CONFIRM_MEETING_TIME.name() + " " + meetingId).build();
        InlineKeyboardButton cancel = InlineKeyboardButton.builder()
                .text(Emojis.CANCEL.getEmojiSpace() + "Не смогу прийти")
                .callbackData(UpcomingState.UPCOMING_CANCEL_MEETING_TIME.name() + " " + meetingId).build();
        keyboard.add(List.of(cancel, confirm));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }


    public InlineKeyboardMarkup getMeetingUpcomingMarkup(List<Meeting> meetings) {
        return InlineKeyboardMarkup.builder().build();
    }
}
