package com.example.demoapp;

import com.example.demoapp.entry_db.*;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Pair;
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
    protected TextView selectedSymptomsText;
    protected ArrayList<Pair<String, String>> selectedSymptomsList = new ArrayList<>();
    protected Button buttonSelectSymptoms;

    // Triggers
    protected TextView selectedTriggersText;
    protected ArrayList<Pair<String, String>> selectedTriggersList = new ArrayList<>();
    protected Button buttonSelectTriggers;

    // Time + Date
    protected TextView timeValue, dateValue;
    protected Calendar selectedTime = Calendar.getInstance();
    protected Button buttonAddSymptoms;

    // Launchers for results
    protected ActivityResultLauncher<Intent> selectSymptomsLauncher;
    protected ActivityResultLauncher<Intent> selectTriggersLauncher;

    protected EntryLogRepository entryLogRepository;

    private String recorder;

    public ChildSymptomsFragment(String recorder){
        this.recorder = recorder;
    }

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

        entryLogRepository = new EntryLogRepository();

        // Connect views
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

        // Initialize TextViews
        setNoSelectionText();

        // ============================
        // SYMPTOMS RESULT HANDLER
        // ============================
        selectSymptomsLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == getActivity().RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null) {
                                    ArrayList<Bundle> bundleList = data.getParcelableArrayListExtra("selectedSymptoms");
                                    if (bundleList != null) {
                                        selectedSymptomsList.clear();
                                        for (Bundle b : bundleList) {
                                            selectedSymptomsList.add(new Pair<>(b.getString("category"), b.getString("name")));
                                        }
                                        selectedSymptomsText.setText(formatPairList(selectedSymptomsList));
                                    }
                                }
                            }
                        });

        // Open SelectSymptomsActivity
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
                            if (result.getResultCode() == getActivity().RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null) {
                                    ArrayList<Bundle> bundleList = data.getParcelableArrayListExtra("selectedTriggers");
                                    if (bundleList != null) {
                                        selectedTriggersList.clear();
                                        for (Bundle b : bundleList) {
                                            selectedTriggersList.add(new Pair<>(b.getString("category"), b.getString("name")));
                                        }
                                        selectedTriggersText.setText(formatPairList(selectedTriggersList));
                                    }
                                }
                            }
                        });

        // Open SelectTriggersActivity
        buttonSelectTriggers.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SelectTriggersActivity.class);
            selectTriggersLauncher.launch(intent);
        });

        // Time picker
        timeValue.setOnClickListener(v -> openTimePicker());

        // Date picker
        dateValue.setOnClickListener(v -> openDatePicker());

        // Add entry
        buttonAddSymptoms.setOnClickListener(v -> saveEntryToFirebase());
    }

    protected String formatPairList(ArrayList<Pair<String, String>> list) {
        if (list.isEmpty()) return "None selected";
        String formattedStr = "";
        for (Pair<String, String> p : list) {
            formattedStr += (p.second + ", ");
        }
        return formattedStr.substring(0, formattedStr.length() - 2);
    }

    protected void openTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (view, hour, minute) -> {
                    timeValue.setText(String.format("%02d:%02d", hour, minute));
                    selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                    selectedTime.set(Calendar.MINUTE, minute);
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void openDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    dateValue.setText(String.format("%02d.%02d.%04d", day, month + 1, year));
                    selectedTime.set(Calendar.YEAR, year);
                    selectedTime.set(Calendar.MONTH, month);
                    selectedTime.set(Calendar.DAY_OF_MONTH, day);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    protected void saveEntryToFirebase() {
        if (selectedSymptomsList.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one symptom", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTriggersList.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one trigger", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = selectedTime.getTimeInMillis();
        EntryLog entry = new EntryLog(selectedSymptomsList, selectedTriggersList, timestamp, recorder);

        buttonAddSymptoms.setEnabled(false);

        entryLogRepository.saveEntry(entry,
                id -> {
                    Toast.makeText(requireContext(), "Entry saved successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                    buttonAddSymptoms.setEnabled(true);
                },
                e -> {
                    Toast.makeText(requireContext(), "Failed to save entry: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    buttonAddSymptoms.setEnabled(true);
                });
    }

    protected void clearForm() {
        selectedSymptomsList.clear();
        selectedTriggersList.clear();
        setNoSelectionText();
        selectedTime = Calendar.getInstance();
    }

    protected void setNoSelectionText(){
        selectedSymptomsText.setText("None selected");
        selectedTriggersText.setText("None selected");
        timeValue.setText("Tap to select time");
        dateValue.setText("Tap to select date");
    }
}
