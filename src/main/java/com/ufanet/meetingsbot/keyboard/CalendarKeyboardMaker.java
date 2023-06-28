package com.ufanet.meetingsbot.keyboard;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import com.ufanet.meetingsbot.utils.Emojis;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CalendarKeyboardMaker extends KeyboardMaker {
    private final String[] dayOfWeek = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};

    private final int startWorkDay = 9;
    private final int endWorkDay = 18;

    public List<List<InlineKeyboardButton>> getCalendarInlineMarkup(MeetingDto meetingDto, String callback, String zoneId) {
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
            date = LocalDate.now(ZoneId.of(zoneId));
        }
        setMonthHeaderCalendar(rowsInLine, date, zoneId);
        setDaysOfWeeksHeaderCalendar(rowsInLine);
        setDaysOfMonthCalendar(rowsInLine, meetingDto, date, zoneId);

        return rowsInLine;
    }

    public List<List<InlineKeyboardButton>> getTimeInlineMarkup(MeetingDto meetingDto, String zoneId) {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

//        List<MeetingDate> meetingDates = meeting.getDates().stream()
//                .sorted().toList();
        LocalDateTime currentDate = LocalDateTime.now(ZoneId.of(zoneId));
        Map<LocalDate, Set<ZonedDateTime>> datesMap = meetingDto.getDatesMap();
        for (Map.Entry<LocalDate, Set<ZonedDateTime>> entry : datesMap.entrySet()) {
            LocalDate localDate = entry.getKey();
            InlineKeyboardButton dateHeader =
                    InlineKeyboardButton.builder()
                            .text(Emojis.CALENDAR.getEmoji() + " " + localDate.format(CustomFormatter.DATE_WEEK_FORMATTER))
                            .callbackData(" ")
                            .build();

            Set<ZonedDateTime> zonedDateTimes = entry.getValue().stream()
                    .map(t -> t.withZoneSameInstant(ZoneId.of(zoneId))).collect(Collectors.toSet());


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

                    ZonedDateTime of = ZonedDateTime.of(localDate, localTime, ZoneId.of(zoneId));

                    InlineKeyboardButton time =
                            InlineKeyboardButton.builder()
                                    .text(localTime.toString())
//                                    .callbackData(of.format(CustomFormatter.DATE_TIME_ZONE_FORMATTER))
                                    .callbackData(of.toString())
                                    .build();

                    if (zonedDateTimes.contains(of)) {
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
                                        MeetingDto meetingDto, LocalDate chosenDate, String zoneId) {
        LocalDate currentDate = LocalDate.now(ZoneId.of(zoneId));

        if (currentDate.getMonthValue() != chosenDate.getMonthValue()) {
            currentDate = LocalDate.of(chosenDate.getYear(), chosenDate.getMonth(), 1);
        } else if (LocalTime.now(ZoneId.of(zoneId)).isAfter(LocalTime.of(endWorkDay, 0))) {
            currentDate = currentDate.plusDays(1);
        }

        int dayOfMonth = currentDate.getDayOfMonth();
        int dayOfWeek = currentDate.getDayOfWeek().getValue();
        int actualDaysOfMonth = currentDate.lengthOfMonth();

        Set<LocalDate> dateSet = meetingDto.getDatesMap().keySet();

        int difference = dayOfMonth - dayOfWeek + 1;

        while (difference <= actualDaysOfMonth) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (int j = 0; j < 7; j++, dayOfWeek++, difference++) {

                InlineKeyboardButton day =
                        InlineKeyboardButton.builder().text(Integer.toString(difference))
                                .callbackData(currentDate.toString())
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

    private void setMonthHeaderCalendar(List<List<InlineKeyboardButton>> rowsInLine, LocalDate chosenDate, String zoneId) {
        LocalDate currentDate = LocalDate.now(ZoneId.of(zoneId));
        int monthValue = chosenDate.getMonthValue();

        List<InlineKeyboardButton> buttons = new ArrayList<>();

        if (currentDate.getMonthValue() < monthValue) {
            InlineKeyboardButton left =
                    InlineKeyboardButton.builder().text("<<")
                            .callbackData(ToggleButton.PREV.name() + chosenDate.minusMonths(1)).build();
            buttons.add(left);
        }

        InlineKeyboardButton monthName = InlineKeyboardButton.builder()
                .text(chosenDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru")))
                .callbackData(" ").build();
        buttons.add(monthName);

        if (monthValue <= Calendar.DECEMBER) {
            InlineKeyboardButton right =
                    InlineKeyboardButton.builder().text(">>")
                            .callbackData(ToggleButton.NEXT.name() + chosenDate.plusMonths(1)).build();
            buttons.add(right);
        }

        rowsInLine.add(buttons);
    }
}
