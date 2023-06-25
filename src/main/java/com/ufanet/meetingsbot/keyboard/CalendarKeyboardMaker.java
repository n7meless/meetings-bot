package com.ufanet.meetingsbot.keyboard;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.model.MeetingDate;
import com.ufanet.meetingsbot.model.MeetingTime;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import com.ufanet.meetingsbot.utils.Emojis;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CalendarKeyboardMaker extends KeyboardMaker {
    private final String[] dayOfWeek = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};

    private final int startWorkDay = 9;
    private final int endWorkDay = 18;

    public List<List<InlineKeyboardButton>> getCalendarInlineMarkup(Meeting meeting, String callback) {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        LocalDate date;
        //TODO поменять подход проверки
        try {
            if (callback.startsWith(ToggleButton.NEXT.name()) || callback.startsWith(ToggleButton.PREV.name())) {
                date = LocalDate.parse(callback.substring(4), CustomFormatter.DATE_FORMATTER);
            } else {
                date = LocalDate.parse(callback, CustomFormatter.DATE_FORMATTER);
            }
        } catch (Exception e) {
            date = LocalDate.now();
        }
        setMonthHeaderCalendar(rowsInLine, date);
        setDaysOfWeeksHeaderCalendar(rowsInLine);
        setDaysOfMonthCalendar(rowsInLine, meeting, date);

        return rowsInLine;
    }

    public List<List<InlineKeyboardButton>> getTimeInlineMarkup(Meeting meeting) {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        List<MeetingDate> meetingDates = meeting.getDates().stream()
                .sorted().toList();
        LocalDateTime currentDate = LocalDateTime.now();
        for (MeetingDate meetingDate : meetingDates) {

            LocalDate localDate = meetingDate.getDate();
            InlineKeyboardButton dateHeader =
                    InlineKeyboardButton.builder()
                            .text(Emojis.CALENDAR.getEmoji() + " " + localDate.format(CustomFormatter.DATE_WEEK_FORMATTER))
                            .callbackData(" ")
                            .build();

            List<LocalDateTime> dateTimes = meetingDate.getMeetingTimes().stream()
                    .map(MeetingTime::getTime).toList();


            int startWorkDay = this.startWorkDay; // начало рабочего дня
            int endWorkDay = this.endWorkDay; // конец рабочего дня

            //считаем который час нам подходит
            while (currentDate.isAfter(LocalDateTime.of(localDate, LocalTime.of(startWorkDay, 0)))
                    && startWorkDay < endWorkDay) {

                startWorkDay++;
            }

            rowsInLine.add(List.of(dateHeader));
            //сколько рабочих часов для встречи осталось
            //определяем длину inline кнопок
            int buttonLength = endWorkDay - startWorkDay;

            int rows = 2;
            int columns = 5;
            if (buttonLength <= columns) rows = 1;

            for (int i = 0; i < rows; i++) {
                List<InlineKeyboardButton> timeButtons = new ArrayList<>();
                for (int j = 0; j < columns && startWorkDay <= endWorkDay; j++, startWorkDay++) {
                    LocalTime localTime = LocalTime.of(startWorkDay, 0);

                    LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

                    InlineKeyboardButton time =
                            InlineKeyboardButton.builder()
                                    .text(localTime.toString())
                                    .callbackData(localDateTime.format(CustomFormatter.DATE_TIME_FORMATTER))
                                    .build();
                    if (dateTimes.contains(localDateTime)) {
                        time.setText("(" + localTime + ")");
                    }
                    timeButtons.add(time);
                }
                rowsInLine.add(timeButtons);
            }
        }
        return rowsInLine;
    }

    private void setDaysOfMonthCalendar(List<List<InlineKeyboardButton>> rowsInLine,
                                        Meeting meeting, LocalDate chosenDate) {
        LocalDate currentDate = LocalDate.now();

        if (currentDate.getMonthValue() != chosenDate.getMonthValue()) {
            currentDate = LocalDate.of(chosenDate.getYear(), chosenDate.getMonth(), 1);
        } else if (LocalTime.now().isAfter(LocalTime.of(this.endWorkDay, 0))) {
            currentDate = currentDate.plusDays(1);
        }

        int dayOfMonth = currentDate.getDayOfMonth();
        int dayOfWeek = currentDate.getDayOfWeek().getValue();
        int actualDaysOfMonth = currentDate.lengthOfMonth();

        Set<LocalDate> dateSet = meeting.getDates().stream()
                .map(MeetingDate::getDate).collect(Collectors.toSet());

        int difference = dayOfMonth - dayOfWeek + 1;

        while (difference <= actualDaysOfMonth) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (int j = 0; j < 7; j++, dayOfWeek++, difference++) {

                InlineKeyboardButton day =
                        InlineKeyboardButton.builder().text(Integer.toString(difference))
                                .callbackData(CustomFormatter.DATE_FORMATTER.format(currentDate))
                                .build();

                // если дата выбрана, то помечаем
                if (dateSet.contains(currentDate)) {
                    day.setText("(" + difference + ")");
                }
                // проверка ограничений календаря
                if (difference < dayOfMonth || difference > actualDaysOfMonth) {
                    day.setText(" ");
                    day.setCallbackData(" ");
                } else {
                    currentDate = currentDate.plusDays(1);
                }
                buttons.add(day);
            }
            rowsInLine.add(new ArrayList<>(buttons));
        }
    }

    private void setDaysOfWeeksHeaderCalendar(List<List<InlineKeyboardButton>> rowsInLine) {
        List<InlineKeyboardButton> dayOfWeeks = new ArrayList<>();
        for (String s : dayOfWeek) {
            InlineKeyboardButton day =
                    InlineKeyboardButton.builder().text(s).callbackData(" ").build();
            dayOfWeeks.add(day);
        }
        rowsInLine.add(new ArrayList<>(dayOfWeeks));
    }

    private void setMonthHeaderCalendar(List<List<InlineKeyboardButton>> rowsInLine, LocalDate chosenDate) {
        LocalDate currentDate = LocalDate.now();
        int monthValue = chosenDate.getMonthValue();

        List<InlineKeyboardButton> buttons = new ArrayList<>();

        if (currentDate.getMonthValue() < monthValue) {
            InlineKeyboardButton left =
                    InlineKeyboardButton.builder().text("<<")
                            .callbackData(ToggleButton.PREV.name() + chosenDate.minusMonths(1)
                                    .format(CustomFormatter.DATE_FORMATTER)).build();
            buttons.add(left);
        }

        InlineKeyboardButton monthName = InlineKeyboardButton.builder()
                .text(chosenDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru")))
                .callbackData(" ").build();
        buttons.add(monthName);

        if (monthValue <= Calendar.DECEMBER) {
            InlineKeyboardButton right =
                    InlineKeyboardButton.builder().text(">>")
                            .callbackData(ToggleButton.NEXT.name() + chosenDate.plusMonths(1)
                                    .format(CustomFormatter.DATE_FORMATTER)).build();
            buttons.add(right);
        }

        rowsInLine.add(buttons);
    }
}
