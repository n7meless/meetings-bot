package com.ufanet.meetingsbot.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomFormatter {
    private static final Locale DEFAULT_LANGUAGE_TAG = Locale.forLanguageTag("ru");

    public static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("H:mm", DEFAULT_LANGUAGE_TAG);

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static final DateTimeFormatter MONTH_YEAR =
            DateTimeFormatter.ofPattern("LLLL yyyy", DEFAULT_LANGUAGE_TAG);

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm", DEFAULT_LANGUAGE_TAG);

    public static final DateTimeFormatter DATE_WEEK_FORMATTER =
            DateTimeFormatter.ofPattern("EE dd.MM.yyyy", DEFAULT_LANGUAGE_TAG);

    public static final DateTimeFormatter DATE_TIME_WEEK_FORMATTER =
            DateTimeFormatter.ofPattern("EE dd.MM.yyyy H:mm", DEFAULT_LANGUAGE_TAG);

    public static final DateTimeFormatter DATE_TIME_ZONE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm z", DEFAULT_LANGUAGE_TAG);

    public static final DateTimeFormatter GOOGLE_DATE_TIME_ZONE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HmsZ", DEFAULT_LANGUAGE_TAG);
}
