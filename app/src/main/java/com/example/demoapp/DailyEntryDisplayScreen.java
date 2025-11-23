package com.example.demoapp;

import com.example.demoapp.entry_list.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class DailyEntryDisplayScreen extends Fragment {

    private ListView listView;
    private EntryAdapter adapter;
    private ArrayList<Entry> entryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_daily_entries_display_screen, container, false);

        listView = view.findViewById(R.id.entry_list);

        // Sample data
        entryList = new ArrayList<>();
        entryList.add(new Entry("Entry #1", "08:30 AM", "A", "Cough, Wheezing", "Dust, Pollen"));
        entryList.add(new Entry("Entry #2", "09:15 AM", "B", "Shortness of breath", "Cold air"));
        entryList.add(new Entry("Entry #3", "10:00 AM", "C", "Cough", "Smoke"));
        entryList.add(new Entry("Entry #3", "10:00 AM", "C", "Cough", "Smoke"));
        entryList.add(new Entry("Entry #3", "10:00 AM", "C", "Cough", "Smoke"));
        entryList.add(new Entry("Entry #3", "10:00 AM", "C", "Cough", "Smoke"));
        entryList.add(new Entry("Entry #3", "10:00 AM", "C", "Cough", "Smoke"));
        entryList.add(new Entry("Entry #3", "10:00 AM", "C", "Cough", "Smoke"));

        // Set adapter
        adapter = new EntryAdapter(getContext(), entryList);
        listView.setAdapter(adapter);

        return view;
    }
}
