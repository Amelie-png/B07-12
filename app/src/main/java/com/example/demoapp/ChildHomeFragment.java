package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChildHomeFragment extends Fragment {

    private String childId;
    private String parentId;

    private String role;
    private FirebaseFirestore db;
    private boolean popupShown = false;

    public ChildHomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_children_home, container, false);
        db = FirebaseFirestore.getInstance();

        // Retrieve child ID & role from Navigation args
        childId = requireArguments().getString("uid");
        role = requireArguments().getString("role");
        Button btnLogDose = view.findViewById(R.id.btnLogDose);
//TODO: Replace the real madication activity name
        btnLogDose.setOnClickListener(v -> {
            //Intent intent = new Intent(getContext(), MedicationActivity.class);
            //intent.putExtra("uid", childId);
            //intent.putExtra("role", role);
            //startActivity(intent);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
// 1. Find the button
        View btnEmergencyAction = view.findViewById(R.id.btnResultAction);

// 2. Make it work
        btnEmergencyAction.setOnClickListener(v -> {
            sendParentAlertTriageOpenedFromHome();

            Intent i = new Intent(getContext(), TriageActivity.class);
            i.putExtra("uid", childId);
            i.putExtra("role", role);
            startActivity(i);

        });

        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        parentId = doc.getString("parentId");
                    }
                });




        if (childId != null) {

            // Pass both childId and role into ZoneFragment
            Bundle args = new Bundle();
            args.putString("uid", childId);
            args.putString("role", role);

            ZoneFragment zoneFragment = new ZoneFragment();
            zoneFragment.setArguments(args);

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.zoneFragmentContainer, zoneFragment)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!popupShown && childId != null) {
            popupShown = true;
            checkOnboardingPopup();
        }
    }

    private void checkOnboardingPopup() {
        DocumentReference childDoc = db.collection("children").document(childId);

        childDoc.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) return;

            Boolean hasSeen = snapshot.getBoolean("hasSeenOnboardingChild");

            if (hasSeen == null || !hasSeen) {
                showOnboardingPopup();
                childDoc.update("hasSeenOnboardingChild", true);
            }
        });
    }

    private void showOnboardingPopup() {
        if (!isAdded()) return;

        new ChildOnboardingDialog(() -> {})
                .show(getChildFragmentManager(), "childOnboarding");
    }
    private void sendParentAlertTriageOpenedFromHome() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> alert = new HashMap<>();
        alert.put("parentId", parentId);
        alert.put("childId", childId);
        alert.put("message", "Your child has opened the triage tool.");
        alert.put("timestamp", System.currentTimeMillis());

        db.collection("alerts").add(alert);
    }

}
