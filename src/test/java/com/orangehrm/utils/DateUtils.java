package com.orangehrm.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Converts dates to the format configured in OrangeHRM (localisation settings).
 * The application exposes its date format through the input placeholder, e.g. "yyyy-dd-mm",
 * so the format is derived at runtime instead of being hard-coded.
 */
public final class DateUtils {

    private DateUtils() {
    }

    /**
     * Translates an OrangeHRM placeholder (yyyy-dd-mm, dd-mm-yyyy, mm/dd/yyyy ...) to a Java formatter.
     */
    public static DateTimeFormatter formatterFromPlaceholder(String placeholder) {
        String pattern = placeholder.trim()
                .replace("yyyy", "uuuu")
                .replace("mm", "MM");
        return DateTimeFormatter.ofPattern(pattern);
    }

    public static String format(LocalDate date, String placeholder) {
        return date.format(formatterFromPlaceholder(placeholder));
    }
}
