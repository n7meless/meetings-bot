package com.ufanet.meetingsbot.keyboard;

import com.ufanet.meetingsbot.model.MeetingDate;
import com.ufanet.meetingsbot.model.MeetingTime;
import com.ufanet.meetingsbot.utils.Emojis;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.ufanet.meetingsbot.constants.ToggleButton.NEXT;
import static com.ufanet.meetingsbot.constants.ToggleButton.PREV;

@Component
public class CalendarKeyboardMaker {
    private final String[] dayOfWeek = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private final String[] months = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октрябрь", "Ноябрь", "Декабрь"};
    private final String[] times = {"9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

    public List<List<InlineKeyboardButton>> getCalendarInlineMarkup(List<MeetingDate> dates, String callback) {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        LocalDate date;
        if (callback.length() > 5) {
            if (callback.startsWith(NEXT.name()) || callback.startsWith(PREV.name())) {
                date = LocalDate.parse(callback.substring(4, callback.length()), dateFormatter);
            } else {
                date = LocalDate.parse(callback, dateFormatter);
            }
        } else date = LocalDate.now();
        setMonthHeaderCalendar(rowsInLine, date);

        setDaysOfWeeksHeaderCalendar(rowsInLine, "");

        setDaysOfMonthCalendar(rowsInLine, dates, date);
        return rowsInLine;
    }

    //TODO подумать над оптимизацией (передавать Set<> и проверять на наличие даты)
    public List<List<InlineKeyboardButton>> getTimeInlineMarkup(List<MeetingDate> dates) {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();

        Set<LocalDateTime> setOfLocalDateTime =
                dates.stream().map(MeetingDate::getTime)
                        .flatMap(Collection::stream)
                        .map(t -> LocalDateTime.of(t.getDate().getDate(), t.getTime()))
                        .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < dates.size(); i++) {
            MeetingDate meetingDate = dates.get(i);

            //TODO поменять вывод даты на кнопках
            LocalDate localDate = meetingDate.getDate();
            InlineKeyboardButton date =
                    InlineKeyboardButton.builder()
                            .text(Emojis.CALENDAR.getEmoji() + " " + localDate.toString())
                            .callbackData(" ")
                            .build();

            rowsInLine.add(List.of(date));

            int k = 0;
            int buttonLength = times.length;
            for (int j = 0; j < 2; j++) {
                List<InlineKeyboardButton> buttons = new ArrayList<>();
                while (buttons.size() < buttonLength / 2 && k < times.length) {
                    LocalTime localTime = LocalTime.parse(times[k], timeFormatter);

                    LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);

                    if (localDateTime.isAfter(now)) {
                        InlineKeyboardButton time =
                                InlineKeyboardButton.builder()
                                        .text(times[k])
                                        .callbackData(localDateTime.toString())
                                        .build();
                        if (setOfLocalDateTime.contains(localDateTime)) {
                            time.setText("(" + times[k] + ")");
                        }
//                        if (!meetingTimes.isEmpty()) {
//                            Optional<MeetingTime> meetingTime = meetingTimes.stream()
//                                    .filter((t) -> localDateTime.isEqual(LocalDateTime.of(localDate, t.getTime()))).findFirst();
//                            if (!meetingTime.isEmpty()) {
//                                time.setText("(" + times[k] + ")");
//                            }
//                        }

                        buttons.add(time);
                    } else buttonLength--;
                    k++;
                }
                rowsInLine.add(buttons);
            }
        }
        return rowsInLine;
    }

    //TODO подумать над оптимизацией
    @SneakyThrows
    private void setDaysOfMonthCalendar(List<List<InlineKeyboardButton>> rowsInLine,
                                        List<MeetingDate> dates, LocalDate old) {
        LocalDate now = LocalDate.now();
        if (now.getMonthValue() != old.getMonthValue()) {
            now = LocalDate.of(old.getYear(), old.getMonthValue(), 1);
        }
        int dayOfMonth = now.getDayOfMonth();
        int dayOfWeek = now.getDayOfWeek().getValue();
        int actualDaysOfMonth = now.lengthOfMonth();

        int difference = dayOfMonth - dayOfWeek + 1;

        Set<LocalDate> dateSet =
                dates.stream().map(MeetingDate::getDate).collect(Collectors.toSet());

        while (difference < actualDaysOfMonth) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (int j = 0; j < 7; j++, dayOfWeek++, difference++) {

                InlineKeyboardButton day =
                        InlineKeyboardButton.builder().text(Integer.toString(difference))
                                .callbackData(now.toString())
                                .build();

                // если дата выбрана, то помечаем
                if (dateSet.contains(now)) {
                    day.setText("(" + difference + ")");
                }
                // проверка ограничений календаря
                if (difference < dayOfMonth || difference > actualDaysOfMonth) {
                    day.setText(" ");
                    day.setCallbackData(" ");
                } else {
                    now = now.plusDays(1);
                }
                buttons.add(day);
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

    private void setMonthHeaderCalendar(List<List<InlineKeyboardButton>> rowsInLine, LocalDate old) {
        LocalDate now = LocalDate.now();
        int monthValue = old.getMonthValue();

        List<InlineKeyboardButton> buttons = new ArrayList<>();

        if (now.getMonthValue() < monthValue) {
            InlineKeyboardButton left =
                    InlineKeyboardButton.builder().text("<<")
                            .callbackData(PREV.name() + old.minusMonths(1)).build();
            buttons.add(left);
        }
        InlineKeyboardButton monthName = InlineKeyboardButton.builder()
                .text(months[monthValue - 1])
                .callbackData(" ").build();
        buttons.add(monthName);

        if (monthValue <= Calendar.DECEMBER) {
            InlineKeyboardButton right =
                    InlineKeyboardButton.builder().text(">>")
                            .callbackData(NEXT.name() + old.plusMonths(1)).build();

            buttons.add(right);
        }

        rowsInLine.add(buttons);
    }
}
