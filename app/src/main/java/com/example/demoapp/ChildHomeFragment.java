package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demoapp.med.MedicineEntry;
import com.example.demoapp.med.MedicineLogWizardActivity;
import com.example.demoapp.med.MedicineRepository;
import com.example.demoapp.med.MedicineUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChildHomeFragment extends Fragment {

    private String childId;
    private String parentId;
    private String role;

    TextView controllerStreak;
    TextView techniqueStreak;

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

        // Add medicine log from home
        Button btnLogDose = view.findViewById(R.id.btnLogDose);

        btnLogDose.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MedicineLogWizardActivity.class);
            intent.putExtra("childId", childId); //same key as for activity
            intent.putExtra("author", role); //don't change!
            startActivity(intent);
        });

        //Bind streak views
        controllerStreak = view.findViewById(R.id.textChipMed);
        techniqueStreak = view.findViewById(R.id.textChipTech);

        setStreaks();
    }

    private void setStreaks() {
        MedicineRepository repo = new MedicineRepository();
        repo.loadBadges(childId, new MedicineRepository.OnResult<Badge>() {
            @Override
            public void onSuccess(Badge badge) {
                if (badge != null) {
                    controllerStreak.setText(String.valueOf((int) badge.getControllerStreak()));
                    techniqueStreak.setText(String.valueOf((int) badge.getTechniqueStreak()));
                }
                else {
                    controllerStreak.setText("0");
                    techniqueStreak.setText("0");
                }
            }
            @Override
            public void onFailure(Exception e) {
                controllerStreak.setText("0");
                techniqueStreak.setText("0");
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
