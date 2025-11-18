package com.example.demoapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class ChildSymptomsFragment extends Fragment {

    private EditText symptomsInput;
    private TextView timeValue, dateValue;
    private Button buttonAddSymptoms;

    public ChildSymptomsFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_symptoms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        symptomsInput = view.findViewById(R.id.symptomsInput);
        timeValue = view.findViewById(R.id.timeValue);
        dateValue = view.findViewById(R.id.dateValue);
        buttonAddSymptoms = view.findViewById(R.id.buttonAddSymptoms);

        // Time Picker
        timeValue.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int min = c.get(Calendar.MINUTE);

            TimePickerDialog dialog = new TimePickerDialog(
                    requireContext(),
                    (timePicker, selectedHour, selectedMinute) -> {
                        String formatted = String.format("%02d:%02d", selectedHour, selectedMinute);
                        timeValue.setText(formatted);
                    },
                    hour, min, true
            );

            dialog.show();
        });

        // Date Picker
        dateValue.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (datePicker, y, m, d) -> {
                        String formatted = String.format("%02d.%02d.%04d", d, m + 1, y);
                        dateValue.setText(formatted);
                    },
                    year, month, day
            );

            dialog.show();
        });

        // Add button validation
        buttonAddSymptoms.setOnClickListener(v -> {
            String symptoms = symptomsInput.getText().toString().trim();
            String time = timeValue.getText().toString();
            String date = dateValue.getText().toString();

            if (symptoms.isEmpty() ||
                    time.equals("Tap to select time") ||
                    date.equals("Tap to select date")) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Symptoms Saved!", Toast.LENGTH_SHORT).show();

                // Optional: clear inputs
                symptomsInput.setText("");
                timeValue.setText("Tap to select time");
                dateValue.setText("Tap to select date");
            }
        });
    }
}
