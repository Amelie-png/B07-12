package com.example.demoapp;

import com.example.demoapp.entry_list.*;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

public class FilterEntriesScreen extends Fragment {
    private String providerUid;
    private TextInputEditText startDateEditText, endDateEditText;
    private ChipGroup chipGroupSymptoms, chipGroupTriggers;
    private Button btnSymptomsToggle, btnTriggersToggle, btnApplyFilter;
    private ArrayList<Entry> filteredEntry;
    private String childUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_entries_screen, container, false);
        initViews(view);
        //TODO: replace with actual childUid retrieve code
        childUid = "oKaNrSiogbRxH5iCxfjS";
        setupDatePickers();
        setupChipGroupControls();
        setupListView();
        setupApplyButton();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            providerUid = getArguments().getString("uid");
        }

        Log.d("ProviderSymptomsFragment", "providerUid = " + providerUid);
    }

    private void initViews(View view) {

        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);

        chipGroupSymptoms = view.findViewById(R.id.chipGroupSymptoms);
        chipGroupTriggers = view.findViewById(R.id.chipGroupTriggers);

        btnSymptomsToggle = view.findViewById(R.id.filter_by_symptoms_button);
        btnTriggersToggle = view.findViewById(R.id.filter_by_trigger_button);
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);

        // Hide chip groups on start
        chipGroupSymptoms.setVisibility(View.GONE);
        chipGroupTriggers.setVisibility(View.GONE);
    }

    private void setupDatePickers() {
        startDateEditText.setOnClickListener(v -> showDatePicker(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePicker(endDateEditText));
    }

    private void showDatePicker(TextInputEditText targetEditText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String formattedDate = (month + 1) + "/" + dayOfMonth + "/" + year;
                    targetEditText.setText(formattedDate);
                    LocalDate date = LocalDate.of(year, month + 1, dayOfMonth);
                    targetEditText.setTag(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void setupChipGroupControls() {

        btnSymptomsToggle.setOnClickListener(v -> {
            if (chipGroupSymptoms.getVisibility() == View.GONE) {
                chipGroupSymptoms.setVisibility(View.VISIBLE);
            } else {
                chipGroupSymptoms.setVisibility(View.GONE);
            }
        });

        btnTriggersToggle.setOnClickListener(v -> {
            if (chipGroupTriggers.getVisibility() == View.GONE) {
                chipGroupTriggers.setVisibility(View.VISIBLE);
            } else {
                chipGroupTriggers.setVisibility(View.GONE);
            }
        });

        enableChipSelection(chipGroupSymptoms);
        enableChipSelection(chipGroupTriggers);
    }

    private void enableChipSelection(ChipGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            chip.setCheckable(true);
        }
    }

    private void setupApplyButton() {
        btnApplyFilter.setOnClickListener(v -> applyFilters());
    }

    private void applyFilters() {
        chipGroupSymptoms.setVisibility(View.GONE);
        chipGroupTriggers.setVisibility(View.GONE);

        // Collect selected symptoms
        ArrayList<String> selectedSymptoms = new ArrayList<>();
        getCheckedChips(chipGroupSymptoms, selectedSymptoms);

        // Collect selected triggers
        ArrayList<String> selectedTriggers = new ArrayList<>();
        getCheckedChips(chipGroupTriggers, selectedTriggers);

        // Create bundle with filter data
        LocalDate startDate = (LocalDate) startDateEditText.getTag();
        LocalDate endDate = (LocalDate) endDateEditText.getTag();

        if(validateDateRange(startDate, endDate)){
            // Pass filter data to DailyEntryDisplayScreen
            Bundle bundle = new Bundle();
            bundle.putSerializable("startDate", startDate);
            bundle.putSerializable("endDate", endDate);
            bundle.putStringArrayList("symptoms", selectedSymptoms);
            bundle.putStringArrayList("triggers", selectedTriggers);
            bundle.putString("childId", childUid);

            // Add filtered entries
            filteredEntry.clear();
            DailyEntryDisplayScreen dailyEntryScreen = new DailyEntryDisplayScreen();
            dailyEntryScreen.setArguments(bundle);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.filtered_entry_list, dailyEntryScreen)
                    .commit();
        }
    }

    private void getCheckedChips(ChipGroup group, ArrayList<String> outputList) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            if (chip.isChecked()) {
                outputList.add(chip.getText().toString());
            }
        }
    }

    private void setupListView() {
        filteredEntry = new ArrayList<>();
    }

    private boolean validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            Toast.makeText(requireContext(),
                    "Please select a start date",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (endDate == null) {
            Toast.makeText(requireContext(),
                    "Please select an end date",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (startDate.isAfter(endDate)) {
            Toast.makeText(requireContext(),
                    "End date must be after start date",
                    Toast.LENGTH_SHORT).show();
            startDateEditText.setText("Start Date");
            endDateEditText.setText("End Date");
            startDateEditText.setTag(null);
            endDateEditText.setTag(null);
            return false;
        }
        return true;
    }
}
