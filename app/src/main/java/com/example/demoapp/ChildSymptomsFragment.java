package com.example.demoapp;

import com.example.demoapp.entry_db.*;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChildSymptomsFragment extends Fragment {
    // Symptoms
    protected TextView selectedSymptomsText;
    protected ArrayList<CategoryName> selectedSymptomsList;
    protected Button buttonSelectSymptoms;

    // Triggers
    protected TextView selectedTriggersText;
    protected ArrayList<CategoryName> selectedTriggersList;
    protected Button buttonSelectTriggers;

    // Time + Date
    protected TextView dateValue;
    protected String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    protected Button buttonAddSymptoms;

    // Launchers for results
    protected ActivityResultLauncher<Intent> selectSymptomsLauncher;
    protected ActivityResultLauncher<Intent> selectTriggersLauncher;

    protected EntryLogRepository entryLogRepository;

    private String recorder;
    //TODO: replace with correct Uid logic
    private String childUid = "oKaNrSiogbRxH5iCxfjS";

    public ChildSymptomsFragment() { }
    public static ChildSymptomsFragment newInstance(String recorder, String childUid) {
        ChildSymptomsFragment fragment = new ChildSymptomsFragment();
        Bundle args = new Bundle();
        args.putString("role", recorder);
        args.putString("uid", childUid);
        fragment.setArguments(args);
        return fragment;
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
        selectedSymptomsList = new ArrayList<>();
        selectedTriggersList = new ArrayList<>();

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
                                            selectedSymptomsList.add(new CategoryName(b.getString("category"), b.getString("name")));
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
                                            selectedTriggersList.add(new CategoryName(b.getString("category"), b.getString("name")));
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

        // Add entry
        buttonAddSymptoms.setOnClickListener(v -> saveEntryToFirebase());
    }

    private String formatPairList(ArrayList<CategoryName> list) {
        if (list.isEmpty()) return "None selected";
        String formattedStr = "";
        for (CategoryName p : list) {
            formattedStr += (p.getName() + ", ");
        }
        return formattedStr.substring(0, formattedStr.length() - 2);
    }

    private void saveEntryToFirebase() {
        EntryLog entry = new EntryLog(childUid, selectedSymptomsList, selectedTriggersList, today, recorder);

        buttonAddSymptoms.setEnabled(false);

        entryLogRepository.saveEntry(entry,
                requireContext(),
                id-> {
                    clearForm();
                    buttonAddSymptoms.setEnabled(true);
                },
                e -> {
                    Toast.makeText(requireContext(), "Failed to save entry: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    buttonAddSymptoms.setEnabled(true);
                });
    }

    private void clearForm() {
        selectedSymptomsList.clear();
        selectedTriggersList.clear();
        setNoSelectionText();
    }

    private void setNoSelectionText(){
        selectedSymptomsText.setText("None selected");
        selectedTriggersText.setText("None selected");
    }
}
