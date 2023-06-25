package com.ufanet.meetingsbot.utils;

import java.time.format.DateTimeFormatter;

public class CustomFormatter {
    public static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm");

    public static DateTimeFormatter DATE_WEEK_FORMATTER = DateTimeFormatter.ofPattern("EE dd.MM.yyyy");
    public static DateTimeFormatter DATE_TIME_WEEK_FORMATTER = DateTimeFormatter.ofPattern("EE dd.MM.yyyy H:mm");
    public static DateTimeFormatter DATE_TIME_ZONE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm z");
}
