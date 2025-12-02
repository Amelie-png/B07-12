package com.example.demoapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TriageActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private String childId;
    private String parentId;
    private String role;

    private CheckBox chkCantSpeak, chkRetractions, chkBlueLips;
    private CheckBox chkCough, chkChestTight;
    private EditText inputRescue, inputPEF;

    private Button btnCheckTriage, btnBack;
    private LinearLayout resultCard;
    private TextView textRiskTitle, textRiskDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // IMPORTANT: Your actual XML file
        setContentView(R.layout.fragment_triage);

        db = FirebaseFirestore.getInstance();

        childId = getIntent().getStringExtra("uid");
        parentId = getIntent().getStringExtra("parentId");
        role = getIntent().getStringExtra("role");

        bindUI();
        setupListeners();
    }

    private void bindUI() {

        chkCantSpeak = findViewById(R.id.chkCantSpeak);
        chkRetractions = findViewById(R.id.chkRetractions);
        chkBlueLips = findViewById(R.id.chkBlueLips);

        chkCough = findViewById(R.id.chkCough);
        chkChestTight = findViewById(R.id.chkChestTight);

        inputRescue = findViewById(R.id.inputRescueAttempts);
        inputPEF = findViewById(R.id.inputPEF);

        btnCheckTriage = findViewById(R.id.btnCheckTriage);
        btnBack = findViewById(R.id.btnBackHomeTriage);

        resultCard = findViewById(R.id.resultCard);
        textRiskTitle = findViewById(R.id.textRiskTitle);
        textRiskDetails = findViewById(R.id.textRiskDetails);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnCheckTriage.setOnClickListener(v -> calculateTriage());
    }

    private void calculateTriage() {

        boolean red1 = chkCantSpeak.isChecked();
        boolean red2 = chkRetractions.isChecked();
        boolean red3 = chkBlueLips.isChecked();

        boolean mod1 = chkCough.isChecked();
        boolean mod2 = chkChestTight.isChecked();

        int rescue = 0;
        String pefText = inputPEF.getText().toString().trim();
        String rescueText = inputRescue.getText().toString().trim();

        if (!rescueText.isEmpty()) {
            rescue = Integer.parseInt(rescueText);
        }

        int pef = 0;
        if (!pefText.isEmpty()) {
            pef = Integer.parseInt(pefText);
        }

        // --------------- Determine severity -----------------

        String severity;

        if (red1 || red2 || red3) {
            severity = "RED";
        } else if (mod1 || mod2 || rescue > 0) {
            severity = "YELLOW";
        } else {
            severity = "GREEN";
        }

        saveTriage(severity, rescue, pef);
    }

    private void saveTriage(String severity, int rescue, int pef) {

        long now = System.currentTimeMillis();

        Map<String, Object> triageEntry = new HashMap<>();
        triageEntry.put("childId", childId);
        triageEntry.put("parentId", parentId);
        triageEntry.put("severity", severity);
        triageEntry.put("timestamp", now);
        triageEntry.put("rescueAttempts", rescue);
        triageEntry.put("pef", pef);
        triageEntry.put("cantSpeak", chkCantSpeak.isChecked());
        triageEntry.put("retractions", chkRetractions.isChecked());
        triageEntry.put("blueLips", chkBlueLips.isChecked());
        triageEntry.put("cough", chkCough.isChecked());
        triageEntry.put("chestTight", chkChestTight.isChecked());

        // --------------- Save triage entry ------------------

        db.collection("triage").document().set(triageEntry)
                .addOnSuccessListener(unused -> {

                    // 🔥 update lastTriageState (most important)
                    db.collection("children")
                            .document(childId)
                            .update("lastTriageState", severity);

                    // 🔥 Only send alert for RED, and only when it becomes RED now
                    if (severity.equals("RED")) {
                        sendRedAlert();
                    }

                    showResultCard(severity);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save triage", Toast.LENGTH_SHORT).show()
                );
    }

    private void sendRedAlert() {

        if (parentId == null || parentId.isEmpty()) return;

        Map<String, Object> alert = new HashMap<>();
        alert.put("childId", childId);
        alert.put("parentId", parentId);
        alert.put("message", "Your child is in severe condition (RED). Check now.");
        alert.put("timestamp", System.currentTimeMillis());
        alert.put("seen", false);

        db.collection("alerts").add(alert);
    }

    private void showResultCard(String severity) {

        resultCard.setVisibility(View.VISIBLE);

        textRiskTitle.setText(severity);

        if (severity.equals("RED")) {
            textRiskDetails.setText("Severe symptoms detected. Seek help immediately.");
        } else if (severity.equals("YELLOW")) {
            textRiskDetails.setText("Moderate symptoms. Continue monitoring.");
        } else {
            textRiskDetails.setText("Stable condition.");
        }
    }
}
