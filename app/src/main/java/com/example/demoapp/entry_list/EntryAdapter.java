package com.example.demoapp.entry_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import com.example.demoapp.R;
import android.widget.Toast;

public class EntryAdapter extends ArrayAdapter<Entry> {
    private Context context;
    private ArrayList<Entry> entries;

    public EntryAdapter(@NonNull Context context, @NonNull ArrayList<Entry> entries) {
        super(context, 0, entries);
        this.context = context;
        this.entries = entries;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.entry, parent, false);
        }

        Entry currentEntry = entries.get(position);

        TextView entryNumber = listItem.findViewById(R.id.entryNumber);
        TextView timeRecorded = listItem.findViewById(R.id.timeRecorded);
        TextView person = listItem.findViewById(R.id.person);
        TextView symptoms = listItem.findViewById(R.id.symptoms);
        TextView triggers = listItem.findViewById(R.id.triggers);

        entryNumber.setText("Entry #" + currentEntry.getEntryNumber());
        timeRecorded.setText(currentEntry.getTimeRecorded());
        person.setText("By " + currentEntry.getPerson());
        symptoms.setText("Symptoms: " + currentEntry.getSymptoms());
        triggers.setText("Triggers: " + currentEntry.getTriggers());

        return listItem;
    }
}