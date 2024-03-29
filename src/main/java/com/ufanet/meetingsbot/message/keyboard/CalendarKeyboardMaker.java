package com.ufanet.meetingsbot.message.keyboard;

import com.ufanet.meetingsbot.constants.Emojis;
import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.dto.MeetingDateDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.MeetingTimeDto;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class CalendarKeyboardMaker extends KeyboardMaker {

    @Value("${telegram.meetings.startWorkDay}")
    private int startWorkDay;
    @Value("${telegram.meetings.endWorkDay}")
    private int endWorkDay;

    public List<List<InlineKeyboardButton>> getCalendarInlineMarkup(MeetingDto meetingDto,
                                                                    String callback, String zoneId) {

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        LocalDate date;
        try {
            if (callback.startsWith(ToggleButton.NEXT.name()) || callback.startsWith(ToggleButton.PREV.name())) {
                date = LocalDate.parse(callback.substring(4));
            } else {
                date = LocalDate.parse(callback);
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

        LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of(zoneId));


        TreeMap<LocalDate, Set<ZonedDateTime>> datesMap = meetingDto.getDates().stream()
                .collect(toMap(MeetingDateDto::getDate,
                        v -> v.getMeetingTimes().stream().map(MeetingTimeDto::getDateTime)
                                .collect(toSet()), (x, y) -> x, TreeMap::new));

        for (Map.Entry<LocalDate, Set<ZonedDateTime>> entry : datesMap.entrySet()) {
            LocalDate localDate = entry.getKey();
            InlineKeyboardButton dateHeader =
                    defaultInlineButton(Emojis.CALENDAR.getEmoji() + " " +
                            localDate.format(CustomFormatter.DATE_WEEK_FORMATTER), " ");

            Set<ZonedDateTime> zonedDateTimes = entry.getValue().stream()
                    .map(t -> t.withZoneSameInstant(ZoneId.of(zoneId))).collect(Collectors.toSet());


            int startWorkDay = this.startWorkDay;
            int endWorkDay = this.endWorkDay;

            //what time is after current date time
            while (currentDateTime.isAfter(LocalDateTime.of(localDate, LocalTime.of(startWorkDay, 0)))
                    && startWorkDay < endWorkDay) {

                startWorkDay++;
            }
            rowsInLine.add(List.of(dateHeader));

            //length of inline buttons
            int buttonLength = endWorkDay - startWorkDay;

            int rows = 2;
            int columns = 5;
            if (buttonLength <= columns) rows = 1;

            for (int i = 0; i < rows; i++) {
                List<InlineKeyboardButton> timeButtons = new ArrayList<>();
                for (int j = 0; j < columns && startWorkDay <= endWorkDay; j++, startWorkDay++) {
                    LocalTime localTime = LocalTime.of(startWorkDay, 0);

                    ZonedDateTime of = ZonedDateTime.of(localDate, localTime, ZoneId.of(zoneId));

                    InlineKeyboardButton time = defaultInlineButton(localTime.toString(), of.toString());

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

        if (currentDate.getMonthValue() < chosenDate.getMonthValue() || currentDate.getYear() < chosenDate.getYear()) {
            currentDate = LocalDate.of(chosenDate.getYear(), chosenDate.getMonth(), 1);
        } else if (LocalTime.now(ZoneId.of(zoneId)).isAfter(LocalTime.of(endWorkDay, 0))) {
            currentDate = currentDate.plusDays(1);
        }

        int dayOfMonth = currentDate.getDayOfMonth();
        int dayOfWeek = currentDate.getDayOfWeek().getValue();
        int actualDaysOfMonth = currentDate.lengthOfMonth();

        Set<LocalDate> dateSet = meetingDto.getDates().stream().map(MeetingDateDto::getDate)
                .collect(Collectors.toSet());

        int difference = dayOfMonth - dayOfWeek + 1;

        while (difference <= actualDaysOfMonth) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (int j = 0; j < 7; j++, dayOfWeek++, difference++) {

                InlineKeyboardButton day =
                        defaultInlineButton(Integer.toString(difference), currentDate.toString());

                if (dateSet.contains(currentDate)) {
                    day.setText("(" + difference + ")");
                }
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
        String[] dayOfWeeks = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (String dayOfWeek : dayOfWeeks) {
            InlineKeyboardButton day = defaultInlineButton(dayOfWeek, " ");
            buttons.add(day);
        }
        rowsInLine.add(new ArrayList<>(buttons));
    }

    private void setMonthHeaderCalendar(List<List<InlineKeyboardButton>> rowsInLine,
                                        LocalDate chosenDate, String zoneId) {
        LocalDate currentDate = LocalDate.now(ZoneId.of(zoneId));
        int month = currentDate.getMonthValue();
        int year = currentDate.getYear();

        int chosenMonth = chosenDate.getMonthValue();
        int chosenYear = chosenDate.getYear();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        if (month < chosenMonth || year < chosenYear) {
            InlineKeyboardButton leftBtn =
                    defaultInlineButton("<<", ToggleButton.PREV.name() + chosenDate.minusMonths(1));
            buttons.add(leftBtn);
        }

        InlineKeyboardButton monthName =
                defaultInlineButton(chosenDate.format(CustomFormatter.MONTH_YEAR), " ");

        buttons.add(monthName);

        InlineKeyboardButton rightBtn =
                defaultInlineButton(">>", ToggleButton.NEXT.name() + chosenDate.plusMonths(1));

        buttons.add(rightBtn);

        rowsInLine.add(buttons);
    }
}
