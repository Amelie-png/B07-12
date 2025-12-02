package com.example.demoapp;

import com.example.demoapp.DailyEntryDisplayScreen;
import com.example.demoapp.calendar.CalendarUtils;
import com.example.demoapp.calendar.CalendarAdapter;

import android.content.Intent;
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

    private String childUid;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;

    Button prevMonthButt;
    Button nextMonthButt;
    Button browseHistoryButton;
    String role;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary_calendar, container, false);

        initWidgets(view);

        role = getArguments().getString("role");
        if(role.equals("provider") || role.equals("parent")){
            childUid = getArguments().getString("childUid");
        } else{
            childUid = getArguments().getString("uid");
        }

        prevMonthButt = view.findViewById(R.id.prev_month_button);
        nextMonthButt = view.findViewById(R.id.next_month_button);
        browseHistoryButton = view.findViewById(R.id.browse_history);

        prevMonthButt.setOnClickListener(v -> previousMonthAction());
        nextMonthButt.setOnClickListener(v -> nextMonthAction());
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

    public void browseHistoryAction() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), FilterEntriesScreen.class);
            intent.putExtra("childUid", childUid);
            intent.putExtra("role", role);
            intent.putExtra("symptomsAllowed", getArguments().getString("symptomsAllowed"));
            intent.putExtra("triggersAllowed", getArguments().getString("triggersAllowed"));
            getActivity().startActivity(intent);
        }
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
        Intent intent = new Intent(requireActivity(), ViewDailyEntryActivity.class);
        // Pass the filter data as extras
        intent.putExtra("Uid", getArguments().getString("uid"));
        intent.putExtra("role", getArguments().getString("role"));
        intent.putExtra("startDate", date.toString());
        intent.putExtra("endDate", date.toString());
        intent.putStringArrayListExtra("symptoms", new ArrayList<String>());
        intent.putStringArrayListExtra("triggers", new ArrayList<String>());
        intent.putExtra("childUid", childUid);
        intent.putExtra("symptomsAllowed", getArguments().getBoolean("symptomsAllowed"));
        intent.putExtra("triggersAllowed", getArguments().getBoolean("triggersAllowed"));
        startActivity(intent);
    }
}
