package com.example.demoapp;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TriageActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private String childId;
    private String role;
    private String parentId;

    private CheckBox chkCantSpeak, chkRetractions, chkBlueLips;
    private CheckBox chkCough, chkChestTight;
    private EditText inputRescueAttempts, inputPEF;
    private String lastTriageState; // "GREEN", "YELLOW", "RED"


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_triage);

        db = FirebaseFirestore.getInstance();

        // Receive args
        Intent intent = getIntent();
        childId = intent.getStringExtra("uid");
        role = intent.getStringExtra("role");

        if (childId == null) {
            Toast.makeText(this, "Missing child ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load parentId
        db.collection("children").document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        parentId = doc.getString("parentId");
                    }
                    lastTriageState = doc.getString("lastTriageState");
                    if (lastTriageState == null) lastTriageState = "GREEN";  // default

                });

        bindUI();

        // ------------------------------
        // TOP: Back to Home button
        // ------------------------------
        Button btnBackHome = findViewById(R.id.btnBackHomeTriage);
        btnBackHome.setOnClickListener(v -> goHome());

        // ------------------------------
        // System Back button handling (modern Android)
        // ------------------------------
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goHome();
            }
        });
    }

    private void bindUI() {
        chkCantSpeak = findViewById(R.id.chkCantSpeak);
        chkRetractions = findViewById(R.id.chkRetractions);
        chkBlueLips = findViewById(R.id.chkBlueLips);
        chkCough = findViewById(R.id.chkCough);
        chkChestTight = findViewById(R.id.chkChestTight);

        inputRescueAttempts = findViewById(R.id.inputRescueAttempts);
        inputPEF = findViewById(R.id.inputPEF);

        findViewById(R.id.btnCheckTriage).setOnClickListener(v -> processTriage());
    }

    private void processTriage() {

        boolean red1 = chkCantSpeak.isChecked();
        boolean red2 = chkRetractions.isChecked();
        boolean red3 = chkBlueLips.isChecked();

        boolean mod1 = chkCough.isChecked();
        boolean mod2 = chkChestTight.isChecked();

        int rescueAttempts = parseIntSafe(inputRescueAttempts.getText().toString());
        int pef = parseIntSafe(inputPEF.getText().toString());

        String newState;

        if (red1 || red2 || red3 || (pef > 0 && pef < 50) || rescueAttempts >= 3) {
            newState = "RED";
        } else if (mod1 || mod2 || (pef > 0 && pef < 80)) {
            newState = "YELLOW";
        } else {
            newState = "GREEN";
        }

        // ⭐ 将结果存到 Firestore (你之前写的方法)
        storeTriageResult(
                newState,
                pef,
                rescueAttempts,
                red1, red2, red3,
                mod1, mod2
        );

        boolean stateChanged = !newState.equals(lastTriageState);

        if (stateChanged && newState.equals("RED")) {
            sendParentAlert("triage_escalation",
                    "Emergency triggered: child entered RED zone.");
        }

        updateTriageState(newState);

        switch (newState) {
            case "RED": goToEmergencyCard(); break;
            case "YELLOW": goToHomeStepsCard("YELLOW"); break;
            default: goToGreenAdviceCard();
        }
    }



    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return 0; }
    }

    private void sendParentAlert(String type, String message) {
        if (parentId == null) return;

        Map<String, Object> alert = new HashMap<>();
        alert.put("parentId", parentId);
        alert.put("childId", childId);
        alert.put("message", "Your child has tried to use the triage tool.");
        alert.put("timestamp", System.currentTimeMillis());

        db.collection("alerts").add(alert);
    }


    private void goToEmergencyCard() {
        Intent i = new Intent(this, EmergencyCardActivity.class);
        i.putExtra("childId", childId);
        i.putExtra("parentId", parentId);
        startActivity(i);
        finish();
    }

    private void goToHomeStepsCard(String zone) {
        Intent i = new Intent(this, HomeStepsCardActivity.class);
        i.putExtra("zone", zone);
        startActivity(i);
        finish();
    }

    private void goToGreenAdviceCard() {
        Intent i = new Intent(this, GreenAdviceCardActivity.class);
        startActivity(i);
        finish();
    }
    private void storeTriageResult(String severity, int pef, int rescueAttempts,
                                   boolean r1, boolean r2, boolean r3,
                                   boolean m1, boolean m2) {

        Map<String, Object> triageRecord = new HashMap<>();
        triageRecord.put("childId", childId);
        triageRecord.put("parentId", parentId);
        triageRecord.put("severity", severity);   // GREEN / YELLOW / RED
        triageRecord.put("pef", pef);
        triageRecord.put("rescueAttempts", rescueAttempts);

        triageRecord.put("cantSpeak", r1);
        triageRecord.put("retractions", r2);
        triageRecord.put("blueLips", r3);
        triageRecord.put("cough", m1);
        triageRecord.put("chestTight", m2);

        triageRecord.put("timestamp", System.currentTimeMillis());

        db.collection("triage").add(triageRecord);
    }


    // ------------------------------
    // GO HOME
    // ------------------------------
    private void goHome() {
        Intent i = new Intent(this, MainNavActivity.class);
        i.putExtra("uid", childId);
        i.putExtra("role", "child");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
    private void updateTriageState(String newState) {
        lastTriageState = newState; // update local copy

        db.collection("children")
                .document(childId)
                .update("lastTriageState", newState)
                .addOnFailureListener(e ->
                        Toast.makeText(TriageActivity.this,
                                "Failed to update triage state",
                                Toast.LENGTH_SHORT).show()
                );
    }


}
