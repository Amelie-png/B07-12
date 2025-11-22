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

    public static void setDateHighlight(LocalDate currentDate, @NonNull CalendarViewHolder cell){
        if (selectedDate != null && currentDate.equals(selectedDate)) {
            cell.dayOfMonth.setBackgroundResource(R.drawable.calendar_today_highlight);
        } else{
            cell.dayOfMonth.setBackgroundResource(0);
        }
    }
}
