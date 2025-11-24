package com.example.demoapp.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;

import java.util.ArrayList;
import java.time.*;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<String> days;
    private final OnItemListener onItemListener;
    private LocalDate today = LocalDate.now();
    private LocalDate selectedDate = LocalDate.now();

    public CalendarAdapter(ArrayList<String> days, OnItemListener onItemListener, LocalDate today, LocalDate selectedDate) {
        this.days = days;
        this.onItemListener = onItemListener;
        this.selectedDate = selectedDate;
        this.today = today;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.fragment_summary_calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (days.size() >= 8) {
            layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        } else {
            layoutParams.height = (int) (parent.getHeight());
        }
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder cell, int position) {
        String dayText = days.get(position);
        if (dayText.contains("\n")) {
            dayText = dayText.split("\n")[1];
        }
        cell.dayOfMonth.setText(dayText);

        if (!dayText.equals("")) {
            int displayYear = selectedDate.getYear();
            int displayMonth = selectedDate.getMonthValue();
            int day = Integer.parseInt(dayText);
            LocalDate currentDate = LocalDate.of(displayYear, displayMonth, day);
            CalendarUtils.setDateHighlight(currentDate, cell);
        } else {
            cell.dayOfMonth.setBackgroundResource(0); // empty cell
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, String dayText);
    }
}