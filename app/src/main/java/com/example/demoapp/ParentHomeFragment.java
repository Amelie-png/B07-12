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

public class ParentHomeFragment extends Fragment {

    private String childId;
    private String parentId;

    private FirebaseFirestore db;

    private TextView emergencyText;
    private Button btnViewTriageHistory;

    private ListenerRegistration alertListener;

    public ParentHomeFragment() {
        // Required empty constructor
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (alertListener != null) {
            alertListener.remove();
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_parent_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // ✅ FIRST: Read the arguments
        if (getArguments() != null) {
            childId = getArguments().getString("uid");
            parentId = getArguments().getString("parentId");
        }

        // ❗ SAFETY CHECK — avoid crash
        if (childId == null) {
            Toast.makeText(requireContext(), "Child ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // 👍 Now childId is GUARANTEED non-null → safe to use
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        parentId = doc.getString("parentId");
                        startListeningForAlerts();
                    }
                });

        bindUI(view);
        loadLatestTriageState();
        loadZoneFragment();
    }

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
    // Load last triage state from "children" doc
    // ---------------------------------------------------------
    private void loadLatestTriageState() {
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String lastState = doc.getString("lastTriageState");
                        if (lastState == null) lastState = "GREEN";
                        updateEmergencyCard(lastState);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load triage state", Toast.LENGTH_SHORT).show()
                );
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
    // Load ZoneFragment inside this fragment
    // ---------------------------------------------------------
    private void loadZoneFragment() {
        ZoneFragment fragment = new ZoneFragment();

        Bundle args = new Bundle();
        args.putString("uid", childId);
        args.putString("role", "parent"); // parent = hide streak, allow history view
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.zoneFragmentContainer, fragment)
                .commit();
    }

    private void startListeningForAlerts() {
        if (parentId == null) return;

        alertListener = db.collection("alerts")
                .whereEqualTo("parentId", parentId)
                .whereEqualTo("seen", false)   // only unread alerts
                .addSnapshotListener((snap, e) -> {


                    if (e != null || snap == null) return;

                    snap.getDocumentChanges().forEach(change -> {

                        if (change.getType() == DocumentChange.Type.ADDED) {

                            String message = change.getDocument().getString("message");
                            String alertId = change.getDocument().getId();

                            // Show popup
                            showAlertPopup(message);

                            // Mark alert as seen
                            db.collection("alerts")
                                    .document(alertId)
                                    .update("seen", true);
                        }
                    });

                });
    }

    private void showAlertPopup(String message) {
        if (!isAdded()) return;

        ParentAlertDialog dialog = new ParentAlertDialog(message);
        dialog.show(getChildFragmentManager(), "parentAlert");
    }

}
