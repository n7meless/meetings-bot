package com.ufanet.meetingsbot.keyboard;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class MeetingInlineKeyboardMaker {
    private final CalendarKeyboardMaker calendarKeyboardMaker;
    private final InlineKeyboardButton stepNext = defaultInlineMarkup("Далее", ToggleButton.NEXT.name());
    private final InlineKeyboardButton cancel = defaultInlineMarkup("Отменить", ToggleButton.CANCEL.name());

    public InlineKeyboardMarkup getCalendarInlineMarkup() {
        return calendarKeyboardMaker.getCalendarInlineMarkup();
    }

    public InlineKeyboardMarkup getTimeOfDiscussionInlineMarkup() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 15; i < 75; i += 15) {
            InlineKeyboardButton button = defaultInlineMarkup(Integer.toString(i), Integer.toString(i));
            row.add(button);
        }
        return InlineKeyboardMarkup.builder().keyboard(List.of(row)).build();
    }

    private InlineKeyboardButton defaultInlineMarkup(String text, String callback) {
        return InlineKeyboardButton.builder().text(text)
                .callbackData(callback).build();
    }

    private List<InlineKeyboardButton> defaultRowHelperInlineMarkup(boolean next) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (next) {
            buttons.add(cancel);
            buttons.add(stepNext);
        }
        else {
            buttons.add(cancel);
        }
        return buttons;
    }

    public InlineKeyboardMarkup getParticipantsInlineMarkup(List<Account> participants) {
        return null;
    }

    public InlineKeyboardMarkup getGroupsInlineMarkup(List<Group> groups) {
        return null;
    }
}
