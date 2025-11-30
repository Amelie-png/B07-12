package com.example.demoapp;

import com.example.demoapp.DailyEntryDisplayScreen;
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

    private String providerUid;
    private String childUid;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;

    Button prevMonthButt;
    Button nextMonthButt;
    Button browseHistoryButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary_calendar, container, false);

        initWidgets(view);

        //TODO: replace with correct childUid logic
        childUid = "oKaNrSiogbRxH5iCxfjS";

        Button prevMonthButton = view.findViewById(R.id.prev_month_button);
        Button nextMonthButton = view.findViewById(R.id.next_month_button);
        Button browseHistoryButton = view.findViewById(R.id.browse_history);

        prevMonthButton.setOnClickListener(v -> previousMonthAction());
        nextMonthButton.setOnClickListener(v -> nextMonthAction());
        browseHistoryButton.setOnClickListener(v -> browseHistoryAction());

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

    public void browseHistoryAction(){
        Fragment filterFragment = new FilterEntriesScreen();

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, filterFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            int day = Integer.parseInt(dayText);
            CalendarUtils.selectedDate = CalendarUtils.selectedDate.withDayOfMonth(day);
            setMonthView();

            openDailyEntryScreen(CalendarUtils.selectedDate);
        }
    }

    private void openDailyEntryScreen(LocalDate date) {
        DailyEntryDisplayScreen fragment = new DailyEntryDisplayScreen();

        // Pass filter data to DailyEntryDisplayScreen
        Bundle bundle = new Bundle();
        bundle.putSerializable("startDate", date);
        bundle.putSerializable("endDate", date);
        bundle.putStringArrayList("symptoms", new ArrayList<>());
        bundle.putStringArrayList("triggers", new ArrayList<>());
        bundle.putString("childId", childUid);
        fragment.setArguments(bundle);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
