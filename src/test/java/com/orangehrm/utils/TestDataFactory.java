package com.orangehrm.utils;

import com.orangehrm.config.ConfigReader;
import com.orangehrm.models.Employee;

import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Generates unique, run-specific test data so tests never depend on
 * permanently hard-coded values and never collide with each other.
 */
public final class TestDataFactory {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

    private TestDataFactory() {
    }

    /** e.g. Automation Test Qa-kxjfwq */
    public static Employee newEmployee() {
        return new Employee(
                ConfigReader.get("test.data.prefix"),
                ConfigReader.get("test.data.middle.name"),
                uniqueLastName());
    }

    /** Letters only, capitalised, e.g. "Qakxjfwqtm". */
    public static String uniqueLastName() {
        return "Qa" + randomLetters(8);
    }

    /** Numeric employee id within the 10 character limit enforced by OrangeHRM. */
    public static String uniqueEmployeeId() {
        long millis = System.currentTimeMillis() % 100_000_000L;
        return String.format("%08d", millis) + RANDOM.nextInt(10);
    }

    public static String randomLetters(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
        }
        return sb.toString();
    }

    public static String uniqueComment() {
        return "Automated leave request " + randomLetters(6);
    }

    /**
     * Picks a random working day (Mon-Fri) between the configured min/max days ahead.
     * Randomising reduces the chance of overlapping with leave left behind by other runs
     * on the shared demo environment. The date is kept inside the current calendar year
     * whenever possible because the demo leave period follows the calendar year.
     */
    public static LocalDate futureWorkingDay() {
        int min = ConfigReader.getInt("leave.min.days.ahead");
        int max = ConfigReader.getInt("leave.max.days.ahead");
        LocalDate today = LocalDate.now();
        LocalDate yearEnd = LocalDate.of(today.getYear(), 12, 31);
        LocalDate latest = today.plusDays(max);
        if (latest.isAfter(yearEnd) && today.plusDays(min).isBefore(yearEnd)) {
            latest = yearEnd;
        }
        int range = (int) Math.max(1, latest.toEpochDay() - today.plusDays(min).toEpochDay());
        LocalDate candidate = today.plusDays(min + RANDOM.nextInt(range));
        return nextWorkingDay(candidate);
    }

    public static LocalDate nextWorkingDay(LocalDate date) {
        LocalDate d = date;
        while (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
            d = d.plusDays(1);
        }
        return d;
    }
}
