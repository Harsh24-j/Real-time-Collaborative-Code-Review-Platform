package com.codereview.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * DateUtil — formatting, relative time, timezone helpers.
 * Skills: Server Side, Spring Boot
 */
public final class DateUtil {

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private DateUtil() {
    }

    public static String toIso(LocalDateTime dt) {
        return dt != null ? dt.format(ISO_FMT) : null;
    }

    public static String toDisplay(LocalDateTime dt) {
        return dt != null ? dt.format(DISPLAY_FMT) : null;
    }

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

    /** Returns a human-readable relative time string, e.g. "3 hours ago". */
    public static String toRelative(LocalDateTime dt) {
        if (dt == null)
            return "unknown";
        LocalDateTime now = LocalDateTime.now();

        long seconds = ChronoUnit.SECONDS.between(dt, now);
        if (seconds < 60)
            return seconds + " seconds ago";

        long minutes = ChronoUnit.MINUTES.between(dt, now);
        if (minutes < 60)
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";

        long hours = ChronoUnit.HOURS.between(dt, now);
        if (hours < 24)
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";

        long days = ChronoUnit.DAYS.between(dt, now);
        if (days < 30)
            return days + " day" + (days == 1 ? "" : "s") + " ago";

        long months = ChronoUnit.MONTHS.between(dt, now);
        if (months < 12)
            return months + " month" + (months == 1 ? "" : "s") + " ago";

        long years = ChronoUnit.YEARS.between(dt, now);
        return years + " year" + (years == 1 ? "" : "s") + " ago";
    }

    public static boolean isWithinHours(LocalDateTime dt, int hours) {
        return dt != null && dt.isAfter(LocalDateTime.now().minusHours(hours));
    }
}
