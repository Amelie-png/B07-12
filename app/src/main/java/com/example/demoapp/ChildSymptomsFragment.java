package com.example.demoapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class ChildSymptomsFragment extends Fragment {

    private String childUid;

    // Symptoms
    private TextView selectedSymptomsText;
    private Button buttonSelectSymptoms;

    // Triggers
    private TextView selectedTriggersText;
    private Button buttonSelectTriggers;

    // Time + Date
    private TextView timeValue, dateValue;
    private Button buttonAddSymptoms;

    // Launchers
    private ActivityResultLauncher<Intent> selectSymptomsLauncher;
    private ActivityResultLauncher<Intent> selectTriggersLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_symptoms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ---- Retrieve UID argument ----
        if (getArguments() != null) {
            childUid = getArguments().getString("uid");
        }
        Log.d("ChildSymptomsFragment", "childUid = " + childUid);

        // Symptoms UI
        buttonSelectSymptoms = view.findViewById(R.id.buttonSelectSymptoms);
        selectedSymptomsText = view.findViewById(R.id.selectedSymptomsText);

        // Triggers UI
        buttonSelectTriggers = view.findViewById(R.id.buttonSelectTriggers);
        selectedTriggersText = view.findViewById(R.id.selectedTriggersText);

        // Time + Date UI
        timeValue = view.findViewById(R.id.timeValue);
        dateValue = view.findViewById(R.id.dateValue);
        buttonAddSymptoms = view.findViewById(R.id.buttonAddSymptoms);

        // ============================
        // SYMPTOMS RESULT HANDLER
        // ============================
        selectSymptomsLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null) {
                                    ArrayList<String> list =
                                            data.getStringArrayListExtra("selectedSymptoms");

                                    if (list != null && !list.isEmpty()) {
                                        selectedSymptomsText.setText(list.toString());
                                    } else {
                                        selectedSymptomsText.setText("No symptoms selected");
                                    }
                                }
                            }
                        });

        buttonSelectSymptoms.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SelectSymptomsActivity.class);
            selectSymptomsLauncher.launch(intent);
        });

        // ============================
        // TRIGGERS RESULT HANDLER
        // ============================
        selectTriggersLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null) {
                                    ArrayList<String> list =
                                            data.getStringArrayListExtra("selectedTriggers");

                                    if (list != null && !list.isEmpty()) {
                                        selectedTriggersText.setText(list.toString());
                                    } else {
                                        selectedTriggersText.setText("No triggers selected");
                                    }
                                }
                            }
                        });

        buttonSelectTriggers.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SelectTriggersActivity.class);
            selectTriggersLauncher.launch(intent);
        });

        // Time picker
        timeValue.setOnClickListener(v -> openTimePicker());

        // Date picker
        dateValue.setOnClickListener(v -> openDatePicker());

        // Add entry
        buttonAddSymptoms.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Entry saved successfully!",
                        Toast.LENGTH_SHORT).show()
        );
    }

    private void openTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (view, hour, minute) ->
                        timeValue.setText(String.format("%02d:%02d", hour, minute)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void openDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (view, year, month, day) ->
                        dateValue.setText(String.format("%02d.%02d.%04d", day, month + 1, year)),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}
