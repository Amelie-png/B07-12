package com.example.demoapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EmergencyCardFragment extends Fragment {

    private String childId;
    private String parentId;

    public EmergencyCardFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            childId = getArguments().getString("childId");
            parentId = getArguments().getString("parentId");
        }

        // Auto-load parentId if missing (child logged in directly)
        if (parentId == null && childId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("children").document(childId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            parentId = doc.getString("parentId");
                        }
                    });
        }
    }

    // ... (onCreateView, onViewCreated)

    private void sendParentAlert(String type, String message) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> alert = new HashMap<>();
        alert.put("childId", childId);
        alert.put("parentId", parentId);
        alert.put("type", type);
        alert.put("message", message);
        alert.put("timestamp", System.currentTimeMillis());

        db.collection("alerts")
                .add(alert)
                .addOnSuccessListener(doc -> {
                    System.out.println("Parent alert saved.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Alert failed: " + e.getMessage());
                });
    }
}
