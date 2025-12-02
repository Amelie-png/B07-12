package com.example.demoapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TriageActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private String childId;
    private String parentId;
    private String role;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triage);

        db = FirebaseFirestore.getInstance();

        childId = getIntent().getStringExtra("uid");
        parentId = getIntent().getStringExtra("parentId");  // may or may not be included
        role = getIntent().getStringExtra("role");

        Button btnSubmit = findViewById(R.id.btnCheckTriage);

        btnSubmit.setOnClickListener(v -> submitTriage());
    }

    /**
     * 1. Computes the severity (GREEN/YELLOW/RED)
     * 2. Writes triage entry to Firestore
     * 3. Updates children/{childId}.lastTriageState = severity
     * 4. Shows success message
     */
    private void submitTriage() {

        // TODO: Replace this with your REAL triage logic
        String severity = calculateSeverity();

        if (severity == null) {
            Toast.makeText(this, "Could not determine severity", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();

        // -----------------------------
        // Save triage entry
        // -----------------------------
        Map<String, Object> entry = new HashMap<>();
        entry.put("childId", childId);
        entry.put("parentId", parentId);
        entry.put("severity", severity);
        entry.put("timestamp", timestamp);

        db.collection("triage")
                .add(entry)
                .addOnSuccessListener(doc -> {

                    // -----------------------------
                    // Save latest severity to child document
                    // -----------------------------
                    db.collection("children")
                            .document(childId)
                            .update("lastTriageState", severity);

                    Toast.makeText(this, "Triage saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save triage", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Replace this with your real triage calculation.
     * This dummy version always returns GREEN.
     */
    private String calculateSeverity() {
        // TODO: Replace with real triage scoring logic
        return "GREEN";
    }

}
