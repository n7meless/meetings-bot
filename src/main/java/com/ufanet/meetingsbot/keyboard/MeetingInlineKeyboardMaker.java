package com.ufanet.meetingsbot.keyboard;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.model.MeetingDate;
import com.ufanet.meetingsbot.model.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class MeetingInlineKeyboardMaker {
    private final CalendarKeyboardMaker calendarKeyboardMaker;
    private final InlineKeyboardButton stepNext = defaultInlineButton("Далее", ToggleButton.NEXT.name());
    private final InlineKeyboardButton cancel = defaultInlineButton("Отменить", ToggleButton.CANCEL.name());
    public InlineKeyboardMarkup getCalendarInlineMarkup(List<MeetingDate> dates, String callback) {
        List<List<InlineKeyboardButton>> keyboard = calendarKeyboardMaker.getCalendarInlineMarkup(dates, callback);
        boolean next = dates.size() > 0;
        List<InlineKeyboardButton> defaultRowHelperInlineMarkup = defaultRowHelperInlineMarkup(next);
        keyboard.add(defaultRowHelperInlineMarkup);
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }
    public InlineKeyboardMarkup getTimeInlineMarkup(List<MeetingDate> dates) {
        List<List<InlineKeyboardButton>> keyboard = calendarKeyboardMaker.getTimeInlineMarkup(dates);
        keyboard.add(defaultRowHelperInlineMarkup(true));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardMarkup getSubjectDurationInlineMarkup() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 15; i <= 90; i += 15) {
            InlineKeyboardButton button = defaultInlineButton(Integer.toString(i), Integer.toString(i));
            row.add(button);
        }
        return InlineKeyboardMarkup.builder().keyboard(List.of(row)).build();
    }

    public InlineKeyboardButton defaultInlineButton(String text, String callback) {
        return InlineKeyboardButton.builder().text(text)
                .callbackData(callback).build();
    }

    public List<InlineKeyboardButton> defaultRowHelperInlineMarkup(boolean next) {
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
                button = List.of(defaultInlineButton("✅ " + participant.getFirstname(), String.valueOf(participant.getId())));
            } else {
                button = List.of(defaultInlineButton(participant.getFirstname(), String.valueOf(participant.getId())));
            }

            keyboard.add(button);
        }
        boolean next = selectedMembers.size() > 0;
        keyboard.add(defaultRowHelperInlineMarkup(next));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardMarkup getGroupsInlineMarkup(List<Group> groups) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (Group group : groups) {
            List<InlineKeyboardButton> button =
                    List.of(defaultInlineButton(group.getTitle(), String.valueOf(group.getId())));

            keyboard.add(button);
        }
        keyboard.add(defaultRowHelperInlineMarkup(false));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public InlineKeyboardMarkup getQuestionsInlineMarkup(List<Question> questions) {
        boolean next = questions.size() > 0;
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(defaultRowHelperInlineMarkup(next)))
                .build();
    }

    public InlineKeyboardMarkup getEditingInlineMarkup() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton sendButton = InlineKeyboardButton.builder().callbackData(ToggleButton.NEXT.name())
                .text("Отправить запрос участникам").build();
        keyboard.add(List.of(sendButton));

        InlineKeyboardButton subject = InlineKeyboardButton.builder().text("Ред. темы").callbackData("Ред. темы").build();
        InlineKeyboardButton participants = InlineKeyboardButton.builder().text("Изм. участников").callbackData("Изм. участников").build();

        keyboard.add(List.of(subject, participants));

        InlineKeyboardButton time = InlineKeyboardButton.builder().text("Изм. время").callbackData("Изм. время").build();
        InlineKeyboardButton address = InlineKeyboardButton.builder().text("Изм. адрес").callbackData("Изм. адрес").build();

        keyboard.add(List.of(time, address));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }
}
