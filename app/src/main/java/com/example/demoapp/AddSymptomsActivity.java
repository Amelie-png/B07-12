package com.example.demoapp;

import com.example.demoapp.entry_db.*;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddSymptomsActivity extends AppCompatActivity {

    // Back Button
    private Button btnBackHome;

    // Symptoms
    private TextView selectedSymptomsText;
    private ArrayList<CategoryName> selectedSymptomsList;
    private Button buttonSelectSymptoms;

    // Triggers
    private TextView selectedTriggersText;
    private ArrayList<CategoryName> selectedTriggersList;
    private Button buttonSelectTriggers;

    // Date
    private Button buttonAddSymptoms;
    private final String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

    // Result launchers
    private ActivityResultLauncher<Intent> selectSymptomsLauncher;
    private ActivityResultLauncher<Intent> selectTriggersLauncher;

    private EntryLogRepository entryLogRepository;

    private String recorder;
    private String childUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_symptoms);

        entryLogRepository = new EntryLogRepository();
        selectedSymptomsList = new ArrayList<>();
        selectedTriggersList = new ArrayList<>();

        // Retrieve passed values from ParentAddSymptomActivity if needed
        if (getIntent() != null) {
            recorder = getIntent().getStringExtra("role");
            if(recorder.equals("parent")){
                childUid = getIntent().getStringExtra("childUid");
            } else{
                childUid = getIntent().getStringExtra("uid");
            }
        }
        Log.d("AddSymptomsActivity", "childUid = " + childUid);

        // Connect views
        btnBackHome = findViewById(R.id.btnBackHome);
        buttonSelectSymptoms = findViewById(R.id.buttonSelectSymptoms);
        selectedSymptomsText = findViewById(R.id.selectedSymptomsText);

        buttonSelectTriggers = findViewById(R.id.buttonSelectTriggers);
        selectedTriggersText = findViewById(R.id.selectedTriggersText);

        buttonAddSymptoms = findViewById(R.id.buttonAddSymptoms);

        setNoSelectionText();

        btnBackHome.setOnClickListener(v -> finish());

        selectSymptomsLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null) {
                                    ArrayList<Bundle> bundleList = data.getParcelableArrayListExtra("selectedSymptoms");
                                    if (bundleList != null) {
                                        selectedSymptomsList.clear();
                                        for (Bundle b : bundleList) {
                                            selectedSymptomsList.add(
                                                    new CategoryName(b.getString("category"), b.getString("name"))
                                            );
                                        }
                                        selectedSymptomsText.setText(formatPairList(selectedSymptomsList));
                                    }
                                }
                            }
                        });

        buttonSelectSymptoms.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectSymptomsActivity.class);
            selectSymptomsLauncher.launch(intent);
        });

        selectTriggersLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null) {
                                    ArrayList<Bundle> bundleList = data.getParcelableArrayListExtra("selectedTriggers");
                                    if (bundleList != null) {
                                        selectedTriggersList.clear();
                                        for (Bundle b : bundleList) {
                                            selectedTriggersList.add(
                                                    new CategoryName(b.getString("category"), b.getString("name"))
                                            );
                                        }
                                        selectedTriggersText.setText(formatPairList(selectedTriggersList));
                                    }
                                }
                            }
                        });

        buttonSelectTriggers.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectTriggersActivity.class);
            selectTriggersLauncher.launch(intent);
        });

        // Add entry to Firebase
        buttonAddSymptoms.setOnClickListener(v -> saveEntryToFirebase());
    }

    private String formatPairList(ArrayList<CategoryName> list) {
        if (list.isEmpty()) return "None selected";
        String formattedStr = "";
        for (CategoryName p : list) {
            formattedStr += ((p.getName()) + (", "));
        }
        return formattedStr.substring(0, formattedStr.length() - 2);
    }

    private void saveEntryToFirebase() {
        EntryLog entry = new EntryLog(childUid, selectedSymptomsList, selectedTriggersList, today, recorder);

        buttonAddSymptoms.setEnabled(false);

        entryLogRepository.saveEntry(
                entry,
                this,
                id -> {
                    clearForm();
                    buttonAddSymptoms.setEnabled(true);
                },
                e -> {
                    Toast.makeText(this, "Failed to save entry: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    buttonAddSymptoms.setEnabled(true);
                }
        );
    }

    private void clearForm() {
        selectedSymptomsList.clear();
        selectedTriggersList.clear();
        setNoSelectionText();
    }

    private void setNoSelectionText() {
        selectedSymptomsText.setText("None selected");
        selectedTriggersText.setText("None selected");
    }
}
