package com.martplus.desktop.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for formatting timestamps into readable user-friendly dates.
 */
public final class DateUtil {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private static final DateTimeFormatter DATE_ONLY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    private DateUtil() {
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DISPLAY_FORMAT);
    }

    public static String formatDateOnly(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DATE_ONLY_FORMAT);
    }
}
