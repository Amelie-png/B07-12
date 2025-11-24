package com.example.demoapp;

import com.example.demoapp.calendar.CalendarUtils;
import com.example.demoapp.calendar.CalendarAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

public class SummaryCalendarFragment extends Fragment implements CalendarAdapter.OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary_calendar, container, false);

        initWidgets(view);

        Button prevMonthButt = view.findViewById(R.id.prev_month_button);
        Button nextMonthButt = view.findViewById(R.id.next_month_button);

        prevMonthButt.setOnClickListener(v -> previousMonthAction());
        nextMonthButt.setOnClickListener(v -> nextMonthAction());

        CalendarUtils.selectedDate = LocalDate.now();
        setMonthView();

        return view;
    }

    private void initWidgets(View view)
    {
        calendarRecyclerView = view.findViewById(R.id.summary_calendar);
        monthYearText = view.findViewById(R.id.monthYear);
    }

    private void setMonthView() {
        monthYearText.setText(CalendarUtils.monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<String> daysInMonth = CalendarUtils.daysInMonthArray(CalendarUtils.selectedDate);

        LocalDate today = LocalDate.now();
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this, today, CalendarUtils.selectedDate);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    public void previousMonthAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction() {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            int day = Integer.parseInt(dayText);
            CalendarUtils.selectedDate = CalendarUtils.selectedDate.withDayOfMonth(day);
            setMonthView();

            String message = "Selected Date " + CalendarUtils.monthYearFromDate(CalendarUtils.selectedDate) + " " + day;
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
