package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class ChildHomeFragment extends Fragment {

    private String childId;
    private String parentId;
    private String role;

    private FirebaseFirestore db;

    public ChildHomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();

        return inflater.inflate(R.layout.fragment_children_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Read childId + role from navigation arguments
        childId = requireArguments().getString("uid");
        role = requireArguments().getString("role");

        if (childId == null) {
            Toast.makeText(requireContext(), "Child ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- FIXED BUTTON LOOKUP ---
        Button btnCheckTriage = view.findViewById(R.id.btnCheckTriage);

        if (btnCheckTriage != null) {
            btnCheckTriage.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), TriageActivity.class);
                i.putExtra("uid", childId);
                i.putExtra("role", role);
                i.putExtra("parentId", parentId);
                startActivity(i);
            });


        }

        // Load parentId first (needed for alert logic)
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        parentId = doc.getString("parentId");
                        Boolean hasSeen = doc.getBoolean("hasSeenOnboardingChild");

                        //  If first time — show onboarding
                        if (hasSeen == null || !hasSeen) {
                            showChildOnboardingDialog();
                        }
                        // Now safely load ZoneFragment
                        loadZoneFragment();
                    }
                });
    }



    private void handleCheckNow() {

        db.collection("triage")
                .whereEqualTo("childId", childId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {

                    String severity = null;

                    if (!snap.isEmpty()) {
                        severity = snap.getDocuments().get(0).getString("severity");
                    }

                    // Only send alert if RED
                    if ("RED".equals(severity)) {
                        sendParentAlert();
                    }

                    launchTriageActivity();
                });
    }

    private void sendParentAlert() {

        if (parentId == null) return;

        Map<String, Object> alert = new HashMap<>();
        alert.put("childId", childId);
        alert.put("parentId", parentId);
        alert.put("message", "Your child is in severe condition. Check now.");
        alert.put("timestamp", System.currentTimeMillis());
        alert.put("seen", false);

        db.collection("alerts").add(alert);
    }

    private void launchTriageActivity() {
        Intent i = new Intent(getContext(), TriageActivity.class);
        i.putExtra("uid", childId);
        i.putExtra("role", role);
        i.putExtra("parentId", parentId);
        startActivity(i);
    }

    private void loadZoneFragment() {
        ZoneFragment fragment = new ZoneFragment();

        Bundle args = new Bundle();
        args.putString("uid", childId);
        args.putString("role", role);
        fragment.setArguments(args);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.zoneFragmentContainer, fragment)
                .commit();
    }
    private void showChildOnboardingDialog() {

        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_child_onboarding, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);  // prevent accidental dismiss
        dialog.show();

        Button btnGotIt = dialogView.findViewById(R.id.btnClose);

        btnGotIt.setOnClickListener(v -> {
            // Mark as seen in Firestore
            db.collection("children")
                    .document(childId)
                    .update("hasSeenOnboardingChild", true);

            dialog.dismiss();
        });
    }


}
