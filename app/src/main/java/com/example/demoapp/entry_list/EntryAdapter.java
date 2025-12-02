package com.example.demoapp.entry_list;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import com.example.demoapp.R;
import android.widget.Toast;

public class EntryAdapter extends ArrayAdapter<Entry> {
    private Context context;
    private ArrayList<Entry> entries;
    boolean symptomsAllowed;
    boolean triggersAllowed;

    public EntryAdapter(@NonNull Context context, @NonNull ArrayList<Entry> entries, boolean symptomsAllowed, boolean triggersAllowed) {
        super(context, 0, entries);
        this.symptomsAllowed = symptomsAllowed;
        this.triggersAllowed = triggersAllowed;
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

        if(!symptomsAllowed){
            symptoms.setTextColor(ContextCompat.getColor(context, R.color.gray));;
        }
        if(!triggersAllowed){
            triggers.setTextColor(ContextCompat.getColor(context, R.color.gray));;
        }
        entryNumber.setText("Check-in #" + currentEntry.getEntryNumber());
        timeRecorded.setText(currentEntry.getTimeRecorded());
        person.setText("By " + currentEntry.getPerson());
        symptoms.setText("Symptoms: " + currentEntry.getSymptoms());
        triggers.setText("Triggers: " + currentEntry.getTriggers());

        return listItem;
    }
}