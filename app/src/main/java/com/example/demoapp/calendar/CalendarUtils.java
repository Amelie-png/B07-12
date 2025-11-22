package com.example.demoapp.calendar;

import androidx.annotation.NonNull;

import com.example.demoapp.R;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

public class CalendarUtils {

    public static LocalDate selectedDate = LocalDate.now();

    public static String formattedDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        return date.format(formatter);
    }

    public static String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public static String formattedTime(LocalTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        return time.format(formatter);
    }

    public static ArrayList<String> daysInWeekArray(LocalDate date) {
        ArrayList<String> days = new ArrayList<>();

        LocalDate current = sundayForDate(date);

        if (current == null) return days;

        for (int i = 0; i < 7; i++) {
            days.add(String.valueOf(current.getDayOfMonth())); // Day number only
            current = current.plusDays(1);
        }

        return days;
    }

    public static ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonth = new ArrayList<>();

        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonthCount = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonthCount + dayOfWeek) {
                daysInMonth.add("");
            } else {
                daysInMonth.add(String.valueOf(i - dayOfWeek));
            }
        }

        return daysInMonth;
    }

    public static ArrayList<String> dateArrayWithDayOfWeek(LocalDate date) {
        ArrayList<String> days = new ArrayList<>();
        LocalDate weekStart = date.with(DayOfWeek.SUNDAY);

        for (int i = 0; i < 7; i++) {
            LocalDate current = weekStart.plusDays(i);
            String dayOfWeek = current.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            String dayOfMonth = String.valueOf(current.getDayOfMonth());
            days.add(dayOfWeek + "\n" + dayOfMonth);
        }

        return days;
    }

    private static LocalDate sundayForDate(LocalDate date) {
        LocalDate current = date;
        while (current.getDayOfWeek() != DayOfWeek.SUNDAY) {
            current = current.minusDays(1);
        }
        return current;
    }

    public static void setDateHighlight(LocalDate currentDate, @NonNull CalendarViewHolder cell){
        if (currentDate.equals(selectedDate)) {
            cell.dayOfMonth.setBackgroundResource(R.drawable.calendar_today_highlight);
        } else{
            cell.dayOfMonth.setBackgroundResource(0);
        }
    }
}
