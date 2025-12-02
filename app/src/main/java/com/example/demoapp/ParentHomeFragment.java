package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashSet;
import java.util.Set;

public class ParentHomeFragment extends Fragment {

    private String childId;
    private String parentId;

    private FirebaseFirestore db;

    private TextView emergencyText;
    private Button btnViewTriageHistory;

    private ListenerRegistration alertListener;

    // 🔒 Prevent duplicate popups during this session
    private final Set<String> shownAlerts = new HashSet<>();

    public ParentHomeFragment() {
        // Required empty constructor
    }

    public static ParentHomeFragment newInstance(String childId, String parentId) {
        ParentHomeFragment fragment = new ParentHomeFragment();
        Bundle args = new Bundle();
        args.putString("uid", childId);
        args.putString("parentId", parentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (alertListener != null) {
            alertListener.remove();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            childId = getArguments().getString("uid");
            parentId = getArguments().getString("parentId");  // optional, overwritten next
        }

        if (childId == null) {
            Toast.makeText(requireContext(), "Child ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 Load parentId from Firestore (source of truth)
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {

                        parentId = doc.getString("parentId");

                        if (parentId == null) {
                            Toast.makeText(requireContext(), "Parent ID missing", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // EVERYTHING depends on parentId — run AFTER loading it
                        bindUI(view);
                        loadLatestTriageState();
                        loadZoneFragment();
                        startListeningForAlerts();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load child", Toast.LENGTH_SHORT).show();
                });
    }


    // ---------------------------------------------------------
    // UI Setup
    // ---------------------------------------------------------
    private void bindUI(View view) {
        emergencyText = view.findViewById(R.id.emergencyText);
        btnViewTriageHistory = view.findViewById(R.id.btnViewTriageHistory);

        btnViewTriageHistory.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), TriageHistoryActivity.class);
            i.putExtra("uid", childId);
            startActivity(i);
        });
    }


    // ---------------------------------------------------------
    // Load last triage state
    // ---------------------------------------------------------
    private void loadLatestTriageState() {
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String lastState = doc.getString("lastTriageState");

                        if (lastState == null) {
                            // ❗ No triage yet — show a neutral placeholder
                            emergencyText.setText("Current Status: No Triage Data Yet");
                            return;
                        }

                        updateEmergencyCard(lastState);
                    }
                });
    }


    private void updateEmergencyCard(String state) {
        switch (state) {
            case "RED":
                emergencyText.setText("Current Status: RED (Emergency)");
                break;
            case "YELLOW":
                emergencyText.setText("Current Status: YELLOW (Moderate)");
                break;
            default:
                emergencyText.setText("Current Status: GREEN (Stable)");
        }
    }


    // ---------------------------------------------------------
    // Load zone fragment
    // ---------------------------------------------------------
    private void loadZoneFragment() {
        ZoneFragment fragment = new ZoneFragment();

        Bundle args = new Bundle();
        args.putString("uid", childId);
        args.putString("role", "parent");
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.zoneFragmentContainer, fragment)
                .commit();
    }


    // ---------------------------------------------------------
    // Alert listener (bullet-proof)
    // ---------------------------------------------------------
    private void startListeningForAlerts() {
        if (parentId == null) return;

        alertListener = db.collection("alerts")
                .whereEqualTo("parentId", parentId)
                .whereEqualTo("seen", false)
                .addSnapshotListener((snap, e) -> {

                    if (e != null || snap == null) return;

                    for (DocumentChange change : snap.getDocumentChanges()) {

                        if (change.getType() == DocumentChange.Type.ADDED) {

                            String alertId = change.getDocument().getId();
                            String message = change.getDocument().getString("message");

                            // 🔒 Local duplicate guard (avoids race condition)
                            if (shownAlerts.contains(alertId)) {
                                continue;
                            }
                            shownAlerts.add(alertId);

                            // Show popup
                            showAlertPopup(message);

                            // Mark alert as seen in Firestore
                            db.collection("alerts")
                                    .document(alertId)
                                    .update("seen", true);
                        }
                    }
                });
    }


    private void showAlertPopup(String message) {
        if (!isAdded()) return;

        ParentAlertDialog dialog = new ParentAlertDialog(message);
        dialog.show(getChildFragmentManager(), "parentAlert");
    }
}
