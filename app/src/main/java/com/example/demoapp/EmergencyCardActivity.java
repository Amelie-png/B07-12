package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EmergencyCardActivity extends AppCompatActivity {

    private String childId;
    private String parentId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_emergency);

        // ---------------------------
        // Retrieve arguments
        // ---------------------------
        Intent intent = getIntent();
        childId = intent.getStringExtra("childId");
        parentId = intent.getStringExtra("parentId");

        // Child fallback
        if (childId == null) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                childId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        }

        // Load parentId if needed
        if (parentId == null && childId != null) {
            fetchParentIdFromFirestore(childId);
        }

        // ---------------------------
        // Setup button
        // ---------------------------
        Button backHomeBtn = findViewById(R.id.btnBackHome);

        backHomeBtn.setOnClickListener(v -> {
            finish();  // closes the activity and goes back
        });
    }

    // ---------------------------
    // Load parentId from Firestore
    // ---------------------------
    private void fetchParentIdFromFirestore(String childId) {

        FirebaseFirestore.getInstance()
                .collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        parentId = doc.getString("parentId");
                        Log.d("EmergencyCard", "ParentId loaded: " + parentId);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("EmergencyCard", "Failed to load parentId: " + e.getMessage())
                );
    }

    // ---------------------------
    // Option: Send parent alert
    // ---------------------------
    private void sendParentAlert(String type, String message) {

        if (childId == null || parentId == null) {
            Log.w("EmergencyCard", "Cannot send alert (IDs missing)");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> alert = new HashMap<>();
        alert.put("childId", childId);
        alert.put("parentId", parentId);
        alert.put("type", type);
        alert.put("message", message);
        alert.put("timestamp", System.currentTimeMillis());

        db.collection("alerts")
                .add(alert)
                .addOnSuccessListener(a ->
                        Log.d("EmergencyCard", "Alert sent"))
                .addOnFailureListener(e ->
                        Log.e("EmergencyCard", "Failed alert: " + e.getMessage()));
    }
}
