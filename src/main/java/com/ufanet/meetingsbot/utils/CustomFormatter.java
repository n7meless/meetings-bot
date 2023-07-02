package com.ufanet.meetingsbot.utils;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CustomFormatter {
    public static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("H:mm");

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter MONTH_YEAR =
            DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru"));

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm");

    public static final DateTimeFormatter DATE_WEEK_FORMATTER =
            DateTimeFormatter.ofPattern("EE dd.MM.yyyy");

    public static final DateTimeFormatter DATE_TIME_WEEK_FORMATTER =
            DateTimeFormatter.ofPattern("EE dd.MM.yyyy H:mm");

    public static final DateTimeFormatter DATE_TIME_ZONE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm z");
    public static final DateTimeFormatter GOOGLE_DATE_TIME_ZONE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HmsZ");
}
