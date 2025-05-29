package com.immortals.miniurl.utils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeUtils {

    public static Instant calculateExpiry(Long amount, String unit) {
        return Instant.now()
                .plus(amount, ChronoUnit.valueOf(unit.toUpperCase()));
    }

    // Default ISO formatters
    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    public static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    public static final DateTimeFormatter ISO_TIME_FORMATTER = DateTimeFormatter.ISO_TIME;

    // Common patterns
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /* Current Date/Time */

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static LocalDateTime now(ZoneId zone) {
        return LocalDateTime.now(zone);
    }

    public static ZonedDateTime nowZoned() {
        return ZonedDateTime.now();
    }

    public static ZonedDateTime nowZoned(ZoneId zone) {
        return ZonedDateTime.now(zone);
    }

    public static Instant nowInstant() {
        return Instant.now();
    }

    public static Date nowDate() {
        return new Date();
    }

    /* Formatting */

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_DATETIME_FORMATTER);
    }

    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime.format(formatter);
    }

    public static String format(LocalDate date) {
        return date.format(DEFAULT_DATE_FORMATTER);
    }

    public static String format(LocalDate date, DateTimeFormatter formatter) {
        return date.format(formatter);
    }

    public static String format(ZonedDateTime zonedDateTime, DateTimeFormatter formatter) {
        return zonedDateTime.format(formatter);
    }

    public static String format(Instant instant, ZoneId zone, DateTimeFormatter formatter) {
        return formatter.format(instant.atZone(zone));
    }

    /* Parsing */

    public static LocalDateTime parseToLocalDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DEFAULT_DATETIME_FORMATTER);
    }

    public static LocalDateTime parseToLocalDateTime(String dateTimeStr, DateTimeFormatter formatter) {
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    public static LocalDate parseToLocalDate(String dateStr) {
        return LocalDate.parse(dateStr, DEFAULT_DATE_FORMATTER);
    }

    public static LocalDate parseToLocalDate(String dateStr, DateTimeFormatter formatter) {
        return LocalDate.parse(dateStr, formatter);
    }

    public static ZonedDateTime parseToZonedDateTime(String dateTimeStr, DateTimeFormatter formatter, ZoneId zone) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
        return ZonedDateTime.of(localDateTime, zone);
    }

    /* Conversion */

    public static Date toDate(LocalDateTime dateTime, ZoneId zone) {
        return Date.from(dateTime.atZone(zone)
                .toInstant());
    }

    public static Date toDate(LocalDate date, ZoneId zone) {
        return Date.from(date.atStartOfDay(zone)
                .toInstant());
    }

    public static LocalDateTime toLocalDateTime(Date date, ZoneId zone) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(zone)
                .toLocalDateTime();
    }

    public static LocalDate toLocalDate(Date date, ZoneId zone) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(zone)
                .toLocalDate();
    }

    /* Date Arithmetic */

    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        return dateTime.plusDays(days);
    }

    public static LocalDateTime addMonths(LocalDateTime dateTime, long months) {
        return dateTime.plusMonths(months);
    }

    public static LocalDateTime addYears(LocalDateTime dateTime, long years) {
        return dateTime.plusYears(years);
    }

    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        return dateTime.plusHours(hours);
    }

    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }

    public static LocalDateTime subtractDays(LocalDateTime dateTime, long days) {
        return dateTime.minusDays(days);
    }

    public static LocalDateTime subtractMonths(LocalDateTime dateTime, long months) {
        return dateTime.minusMonths(months);
    }

    public static LocalDateTime subtractYears(LocalDateTime dateTime, long years) {
        return dateTime.minusYears(years);
    }

    public static LocalDateTime subtractHours(LocalDateTime dateTime, long hours) {
        return dateTime.minusHours(hours);
    }

    public static LocalDateTime subtractMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.minusMinutes(minutes);
    }

    /* Start / End of Day, Week, Month, Year */

    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate()
                .atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate()
                .atTime(LocalTime.MAX);
    }

    public static LocalDateTime startOfWeek(LocalDate date, DayOfWeek startDay) {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(startDay))
                .atStartOfDay();
    }

    public static LocalDateTime endOfWeek(LocalDate date, DayOfWeek endDay) {
        return date.with(java.time.temporal.TemporalAdjusters.nextOrSame(endDay))
                .atTime(LocalTime.MAX);
    }

    public static LocalDateTime startOfMonth(LocalDate date) {
        return date.withDayOfMonth(1)
                .atStartOfDay();
    }

    public static LocalDateTime endOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth())
                .atTime(LocalTime.MAX);
    }

    public static LocalDateTime startOfYear(LocalDate date) {
        return date.withDayOfYear(1)
                .atStartOfDay();
    }

    public static LocalDateTime endOfYear(LocalDate date) {
        return date.withDayOfYear(date.lengthOfYear())
                .atTime(LocalTime.MAX);
    }

    /* Difference and Duration */

    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    public static Duration durationBetween(Instant start, Instant end) {
        return Duration.between(start, end);
    }

    /* Date Comparisons */

    public static boolean isBefore(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.isBefore(dateTime2);
    }

    public static boolean isAfter(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.isAfter(dateTime2);
    }

    public static boolean isBetween(LocalDateTime target, LocalDateTime start, LocalDateTime end) {
        return !target.isBefore(start) && !target.isAfter(end);
    }

    /* TimeZone Utilities */

    public static ZoneId getZoneId(String zoneId) {
        return ZoneId.of(zoneId);
    }

    public static ZoneOffset getZoneOffset(String offset) {
        return ZoneOffset.of(offset);
    }

    public static TimeZone getTimeZone(String zoneId) {
        return TimeZone.getTimeZone(zoneId);
    }

    /* Convert to/from epoch seconds */

    public static long toEpochSeconds(LocalDateTime dateTime, ZoneId zone) {
        return dateTime.atZone(zone)
                .toEpochSecond();
    }

    public static long toEpochMilli(LocalDateTime dateTime, ZoneId zone) {
        return dateTime.atZone(zone)
                .toInstant()
                .toEpochMilli();
    }

    public static LocalDateTime fromEpochMilli(long epochMilli, ZoneId zone) {
        return Instant.ofEpochMilli(epochMilli)
                .atZone(zone)
                .toLocalDateTime();
    }

    /* Additional Helpers */

    // Checks if a year is a leap year
    public static boolean isLeapYear(int year) {
        return Year.isLeap(year);
    }

    // Get the number of days in a month for a given year and month
    public static int daysInMonth(int year, int month) {
        return YearMonth.of(year, month)
                .lengthOfMonth();
    }

}
