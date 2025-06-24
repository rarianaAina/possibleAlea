package com.example.erpnextintegration.utils;


import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class DateUtils {


    private static final DateTimeFormatter FORMAT_DDMMYYYY_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMAT_DDMMYYYY_DASH = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FORMAT_YYYYMMDD_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter FORMAT_YYYYMMDD_DASH = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final Pattern PATTERN_DDMMYYYY_SLASH = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");
    private static final Pattern PATTERN_DDMMYYYY_DASH = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$");
    private static final Pattern PATTERN_YYYYMMDD_SLASH = Pattern.compile("^\\d{4}/\\d{2}/\\d{2}$");
    private static final Pattern PATTERN_YYYYMMDD_DASH = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    public static boolean isValidDate(String dateStr) {
        return normalizeToStandardFormat(dateStr) != null;
    }

    public static String getMonthName(String yearMonth) {
        try {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");


            YearMonth date = YearMonth.parse(yearMonth, formatter);

            return date.getMonth()
                    .getDisplayName(TextStyle.FULL, Locale.FRENCH)
                    + " " + date.getYear();
        } catch (Exception e) {
            return "Date invalide";
        }
    }

    public static LocalDate normalizeToStandardFormat(String dateStr) {
        try {
            if(dateStr==null){
                return null;
            }
            if (PATTERN_DDMMYYYY_SLASH.matcher(dateStr).matches()) {
                return validateAndParse(dateStr, FORMAT_DDMMYYYY_SLASH, "DMY", "/");
            } else if (PATTERN_DDMMYYYY_DASH.matcher(dateStr).matches()) {
                return validateAndParse(dateStr, FORMAT_DDMMYYYY_DASH, "DMY", "-");
            } else if (PATTERN_YYYYMMDD_SLASH.matcher(dateStr).matches()) {
                return validateAndParse(dateStr, FORMAT_YYYYMMDD_SLASH, "YMD", "/");
            } else if (PATTERN_YYYYMMDD_DASH.matcher(dateStr).matches()) {
                return validateAndParse(dateStr, FORMAT_YYYYMMDD_DASH, "YMD", "-");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate validateAndParse(String dateStr, DateTimeFormatter formatter, String order, String separator) {
        String[] parts = dateStr.split(Pattern.quote(separator));
        int day, month, year;

        switch (order) {
            case "DMY":
                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                year = Integer.parseInt(parts[2]);
                break;
            case "YMD":
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                day = Integer.parseInt(parts[2]);
                break;
            default:
                return null;
        }

        if (!isValidDateComponents(year, month, day)) {
            return null;
        }

        return LocalDate.parse(dateStr, formatter);
    }

    private static boolean isValidDateComponents(int year, int month, int day) {
        if (month < 1 || month > 12 || day < 1) return false;

        int maxDay;
        switch (month) {
            case 2: maxDay = isLeapYear(year) ? 29 : 28; break;
            case 4: case 6: case 9: case 11: maxDay = 30; break;
            default: maxDay = 31;
        }

        return day <= maxDay;
    }

    private static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    public static String normalizeToString(String dateStr) {
        LocalDate date = normalizeToStandardFormat(dateStr);
        return date != null ? date.format(FORMAT_YYYYMMDD_SLASH) : null;
    }
}