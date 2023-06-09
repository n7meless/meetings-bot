package com.ufanet.meetingsbot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
@Component
public class CalendarKeyboardMaker {
    private final String[] dayOfWeek = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    public InlineKeyboardMarkup getCalendarInlineMarkup() {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        setMonthHeaderCalendar(rowsInLine);

        setDaysOfWeeksHeaderCalendar(rowsInLine, "");

        setDaysOfMonthCalendar(rowsInLine);

        return InlineKeyboardMarkup.builder()
                .keyboard(rowsInLine)
                .build();
    }
    private void setDaysOfMonthCalendar(List<List<InlineKeyboardButton>> rowsInLine) {
        Calendar instance = new GregorianCalendar(Locale.getDefault());
        System.out.println(instance.getTime());
        System.out.println(instance.after(instance));
        int dayOfMonth = instance.get(Calendar.DAY_OF_MONTH); //8
        int dayOfWeek = (instance.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1; //4
        int actualDaysOfMonth = instance.getActualMaximum(Calendar.DAY_OF_MONTH);

        System.out.println(dayOfMonth);
        System.out.println(dayOfWeek);
        System.out.println(actualDaysOfMonth);

        int difference = dayOfMonth - dayOfWeek + 1;
        while (difference < actualDaysOfMonth) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (int j = 0; j < 7; j++, dayOfWeek++) {
                InlineKeyboardButton day =
                        InlineKeyboardButton.builder()
                                .text(Integer.toString(difference))
                                .callbackData("2")
                                .switchInlineQuery("ass")
                                .build();
                if (difference < dayOfMonth || difference > actualDaysOfMonth) {
                    day.setText(" ");
                }
                buttons.add(day);
                difference++;
            }
            rowsInLine.add(new ArrayList<>(buttons));
        }
    }

    private void setDaysOfWeeksHeaderCalendar(List<List<InlineKeyboardButton>> rowsInLine, String lang) {
        List<InlineKeyboardButton> dayOfWeeks = new ArrayList<>();
        for (String s : dayOfWeek) {
            InlineKeyboardButton day =
                    InlineKeyboardButton.builder().text(s).callbackData(" ").build();
            dayOfWeeks.add(day);
        }
        rowsInLine.add(new ArrayList<>(dayOfWeeks));
    }

    private void setMonthHeaderCalendar(List<List<InlineKeyboardButton>> rowsInLine) {
        InlineKeyboardButton left =
                InlineKeyboardButton.builder().text("<<").callbackData(" ").build();
        InlineKeyboardButton month = InlineKeyboardButton.builder()
                .text("Июнь").callbackData(" ").build();
        InlineKeyboardButton right =
                InlineKeyboardButton.builder().text(">>").callbackData(" ").build();

        List<InlineKeyboardButton> dayOfYears = new ArrayList<>(List.of(left, month, right));
        rowsInLine.add(dayOfYears);
    }
}
