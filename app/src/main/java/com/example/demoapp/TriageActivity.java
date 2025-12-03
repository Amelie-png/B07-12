package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

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
    private static final long RECHECK_DELAY = 10 * 60 * 1000; // 10 minutes
    private android.os.Handler recheckHandler = new android.os.Handler();

    private int personalBest = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_triage);

        db = FirebaseFirestore.getInstance();

        childId = getIntent().getStringExtra("uid");
        parentId = getIntent().getStringExtra("parentId");
        role = getIntent().getStringExtra("role");
        personalBest = getIntent().getIntExtra("pef", 0);

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
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCheckTriage.setOnClickListener(v -> calculateTriage());
    }

    private void calculateTriage() {

        boolean isRed =
                chkCantSpeak.isChecked() ||
                        chkRetractions.isChecked() ||
                        chkBlueLips.isChecked();

        boolean isYellow =
                chkCough.isChecked() ||
                        chkChestTight.isChecked() ||
                        (!inputRescue.getText().toString().trim().isEmpty()
                                && Integer.parseInt(inputRescue.getText().toString().trim()) > 0);

        // ---------- PEF LOGIC ----------
        int pefValue = 0;
        String pefText = inputPEF.getText().toString().trim();

        if (!pefText.isEmpty()) {
            pefValue = Integer.parseInt(pefText);
        }

        boolean hasPersonalBest = personalBest > 0;
        boolean severeLowPEF = false;

        if (hasPersonalBest && pefValue > 0) {
            double percentage = (pefValue / (double) personalBest) * 100.0;
            severeLowPEF = percentage < 50.0;
        }

        // ---------- FINAL SEVERITY ----------
        String severity;

        if (severeLowPEF || isRed) {
            severity = "RED";
        } else if (isYellow) {
            severity = "YELLOW";
        } else {
            severity = "GREEN";
        }

        saveTriage(severity, pefValue);
    }


    private void saveTriage(String severity, int pefValue) {

        long now = System.currentTimeMillis();

        Map<String, Object> triageEntry = new HashMap<>();
        triageEntry.put("childId", childId);
        triageEntry.put("parentId", parentId);
        triageEntry.put("severity", severity);
        triageEntry.put("timestamp", now);
        triageEntry.put("pef", pefValue);   // ← ADD THIS

        db.collection("triage").add(triageEntry)
                .addOnSuccessListener(unused -> {

                    // update last state
                    db.collection("children")
                            .document(childId)
                            .update("lastTriageState", severity);

                    // only RED sends parent alert now
                    if (severity.equals("RED")) {
                        sendRedAlert();
                    }

                    // 🎯 Route to UI card activity
                    openCorrectAdviceCard(severity);

                });
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

    private void openCorrectAdviceCard(String severity) {

        Intent i;

        switch (severity) {

            case "RED":
                i = new Intent(this, EmergencyCardActivity.class);
                break;

            case "YELLOW":
                scheduleYellowRecheck();   // <-- ADD THIS
                i = new Intent(this, HomeStepsCardActivity.class);
                break;


            default:
                i = new Intent(this, GreenAdviceCardActivity.class);
        }

        i.putExtra("uid", childId);
        i.putExtra("role", role);

        startActivity(i);
        finish(); // close triage screen
    }
    private void scheduleYellowRecheck() {

        // Cancel any previous timers for safety
        recheckHandler.removeCallbacksAndMessages(null);

        recheckHandler.postDelayed(() -> {

            // Re-check child state from Firestore BEFORE restarting triage
            db.collection("children")
                    .document(childId)
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        if (!snapshot.exists()) return;

                        String state = snapshot.getString("lastTriageState");

                        if (state == null) return;

                        // Only re-open triage if still yellow
                        if (state.equals("YELLOW")) {

                            Intent i = new Intent(this, TriageActivity.class);
                            i.putExtra("uid", childId);
                            i.putExtra("parentId", parentId);
                            i.putExtra("role", role);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(i);

                        } else {
                            // If RED or GREEN, stop rechecking
                            recheckHandler.removeCallbacksAndMessages(null);
                        }

                    });

        }, RECHECK_DELAY);
    }

}
