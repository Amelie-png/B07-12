package com.example.demoapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EmergencyCardFragment extends Fragment {

    private String childId;
    private String parentId;

    public EmergencyCardFragment() {}

    // -------------------------------
    // Load arguments early (best practice)
    // -------------------------------
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve from arguments (if passed by parent)
        if (getArguments() != null) {
            childId = getArguments().getString("childId");
            parentId = getArguments().getString("parentId");
        }

        // If child logs in directly, fallback to their UID
        if (childId == null) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                childId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        }

        // If parentId still missing → fetch from Firestore
        if (parentId == null && childId != null) {
            fetchParentIdFromFirestore(childId);
        }
    }

    // -------------------------------
    // Inflate layout
    // -------------------------------
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.card_emergency, container, false);
    }

    // -------------------------------
    // Setup UI listeners
    // -------------------------------
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button backHomeBtn = view.findViewById(R.id.btnBackHome);

        backHomeBtn.setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );
    }

    // -------------------------------
    // Fetch parentId from Firestore if missing
    // -------------------------------
    private void fetchParentIdFromFirestore(String childId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("children").document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        parentId = doc.getString("parentId");
                        System.out.println("Loaded parentId from Firestore: " + parentId);
                    } else {
                        System.out.println("Child document not found.");
                    }
                })
                .addOnFailureListener(e ->
                        System.out.println("Failed to load parentId: " + e.getMessage())
                );
    }

    // -------------------------------
    // Send alert to parent
    // -------------------------------
    private void sendParentAlert(String type, String message) {

        if (childId == null) {
            System.out.println(" Cannot send alert: childId is null");
            return;
        }
        if (parentId == null) {
            System.out.println(" Cannot send alert: parentId is null (still loading?)");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> alert = new HashMap<>();
        alert.put("childId", childId);
        alert.put("parentId", parentId);
        alert.put("type", type);           // triage_start / triage_escalation
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