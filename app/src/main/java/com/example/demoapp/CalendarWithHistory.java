package com.example.demoapp;

import com.example.demoapp.calendar.CalendarUtils;
import com.example.demoapp.calendar.CalendarAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarWithHistory extends Fragment{

    private String childUid;
    private FrameLayout displayPlainCalendarContainer;
    Button browseHistoryButton;
    String role;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary_calendar_with_history, container, false);

        initWidgets(view);

        role = getArguments().getString("role");
        if(role.equals("provider") || role.equals("parent")){
            childUid = getArguments().getString("childUid");
        } else{
            childUid = getArguments().getString("uid");
        }

        browseHistoryButton = view.findViewById(R.id.browse_history);
        browseHistoryButton.setOnClickListener(v -> browseHistoryAction());

        if (savedInstanceState == null) {
            PlainCalendar calendarFragment = new PlainCalendar();

            Bundle calendarArgs = new Bundle();
            calendarArgs.putString("childUid", childUid);
            calendarArgs.putString("role", role);
            calendarFragment.setArguments(calendarArgs);

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.display_plain_calendar_container, calendarFragment)
                    .commit();
        }

        return view;
    }

    private void initWidgets(View view)
    {
        displayPlainCalendarContainer = view.findViewById(R.id.display_plain_calendar_container);
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
}
