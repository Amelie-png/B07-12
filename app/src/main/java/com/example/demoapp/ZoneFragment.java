package com.example.demoapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ZoneFragment extends Fragment {

    private FirebaseFirestore db;

    private String childId;
    private String role;
    private String parentId;

    // UI
    private LinearLayout layoutNoZone, layoutHasZone, layoutNoPB;
    private TextView textZonePercent;
    private View zoneIndicator;
    private ImageView zoneBar;
    private Button btnAddZone, btnEditZone, btnZoneHistory;
    private boolean redAlertShown = false;

    // State
    private int personalBest = -1;
    private boolean lastBeforeMed = false;
    private boolean lastAfterMed = false;

    public ZoneFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_zone, container, false);

        db = FirebaseFirestore.getInstance();

        // Bind UI
        layoutNoZone = v.findViewById(R.id.layoutNoPEF);
        layoutHasZone = v.findViewById(R.id.layoutHasPEF);
        layoutNoPB = v.findViewById(R.id.layoutNoPB);

        textZonePercent = v.findViewById(R.id.textZonePercent);
        zoneIndicator = v.findViewById(R.id.pefIndicator);
        zoneBar = v.findViewById(R.id.pefBar);

        btnAddZone = v.findViewById(R.id.btnAddPEF);
        btnEditZone = v.findViewById(R.id.btnEditPEF);
        btnZoneHistory = v.findViewById(R.id.btnZoneHistory);

        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // LOAD ARGS
        Bundle args = requireArguments();
        childId = args.getString("uid");
        role = args.getString("role");

        applyRoleUI();   // hides or shows things based on role

        btnZoneHistory.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), ZoneHistoryActivity.class);
            i.putExtra("uid", childId);
            startActivity(i);
        });

        btnAddZone.setOnClickListener(v -> showAddZoneDialog());
        btnEditZone.setOnClickListener(v -> showEditZoneDialog());

        // Load PB and zone
        loadPersonalBest();

        // Load parentId if needed later
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) parentId = doc.getString("parentId");
                });
    }


    // ---------------------------------------------------------
    // ROLE UI (single place, no duplicates)
    // ---------------------------------------------------------
    private void applyRoleUI() {

        // DEFAULTS — hidden history for non-parents
        btnZoneHistory.setVisibility(View.GONE);

        if (role == null) return;

        switch (role.toLowerCase()) {

            case "parent":
                btnAddZone.setVisibility(View.VISIBLE);
                btnEditZone.setVisibility(View.VISIBLE);
                btnZoneHistory.setVisibility(View.VISIBLE);
                break;

            case "child":
                // Child: no history
                btnZoneHistory.setVisibility(View.GONE);
                break;

            case "provider":
                // Providers cannot add or edit
                btnAddZone.setVisibility(View.GONE);
                btnEditZone.setVisibility(View.GONE);
                btnZoneHistory.setVisibility(View.GONE);
                break;
        }
    }


    // ---------------------------------------------------------
    // PERSONAL BEST
    // ---------------------------------------------------------
    private void loadPersonalBest() {
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists() && doc.contains("pb")) {
                        personalBest = doc.getLong("pb").intValue();
                    }

                    boolean pbMissing = (personalBest <= 0);

                    if (pbMissing) {
                        showNoPBLayout();
                    } else {
                        loadTodayZone();
                    }

                });
    }


    private void showNoPBLayout() {
        layoutNoPB.setVisibility(View.VISIBLE);
        layoutNoZone.setVisibility(View.GONE);
        layoutHasZone.setVisibility(View.GONE);

        // Child cannot add/edit until PB exists
        if ("child".equalsIgnoreCase(role)) {
            btnAddZone.setVisibility(View.GONE);
            btnEditZone.setVisibility(View.GONE);
        }
        if ("parent".equalsIgnoreCase(role)) {
            btnAddZone.setVisibility(View.GONE);
        }
    }


    // ---------------------------------------------------------
    // LOAD TODAY'S LATEST PEF
    // ---------------------------------------------------------
    private void loadTodayZone() {

        long todayStart = getTodayStartMillis();

        db.collection("zone")
                .whereEqualTo("childId", childId)
                .get()
                .addOnSuccessListener(query -> {

                    DocumentSnapshot latest = null;
                    long latestTs = -1;

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Long ts = doc.getLong("timestamp");
                        if (ts == null) continue;
                        if (ts >= todayStart && ts > latestTs) {
                            latestTs = ts;
                            latest = doc;
                        }
                    }

                    if (latest == null) {
                        showNoZoneLayout();
                        return;
                    }

                    // load flags for edit dialog
                    lastBeforeMed = latest.getBoolean("beforeMed") != null && latest.getBoolean("beforeMed");
                    lastAfterMed = latest.getBoolean("afterMed") != null && latest.getBoolean("afterMed");

                    int pef = latest.getLong("pef").intValue();
                    if (!isAdded()) return;

                    updateZoneUI(pef);
                })
                .addOnFailureListener(e -> showNoZoneLayout());
    }


    private long getTodayStartMillis() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }


    private void showNoZoneLayout() {
        layoutNoZone.setVisibility(View.VISIBLE);
        layoutHasZone.setVisibility(View.GONE);
        layoutNoPB.setVisibility(View.GONE);

        if ("parent".equalsIgnoreCase(role)) {
            btnAddZone.setVisibility(View.VISIBLE);
            btnEditZone.setVisibility(View.GONE);
        } else {
            btnAddZone.setVisibility(View.VISIBLE);
            btnEditZone.setVisibility(View.GONE);
        }
    }


    // ---------------------------------------------------------
    // UPDATE UI WITH ZONE
    // ---------------------------------------------------------
    private void updateZoneUI(int pef) {
        if (!isAdded()) return;  // ← IMPORTANT

        layoutNoZone.setVisibility(View.GONE);
        layoutHasZone.setVisibility(View.VISIBLE);
        layoutNoPB.setVisibility(View.GONE);

        int percent = (int)((pef * 100f) / personalBest);

        // Show history for parents only
        if ("parent".equalsIgnoreCase(role))
            btnZoneHistory.setVisibility(View.VISIBLE);
        else
            btnZoneHistory.setVisibility(View.GONE);

        textZonePercent.setText(percent + "%");

        moveIndicator(percent);
        setPercentColor(percent);
    }


    private void moveIndicator(int percent) {
        // clamp
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;

        int finalPercent = percent;

        zoneBar.post(() -> {
            float barWidth = zoneBar.getWidth();
            float leftPadding = barWidth * 0.07f;
            float rightPadding = barWidth * 0.07f;
            float usable = barWidth - leftPadding - rightPadding;

            float posX = leftPadding + (finalPercent / 100f) * usable;

            zoneIndicator.setX(posX - zoneIndicator.getWidth() / 2f);
            zoneIndicator.setY(zoneBar.getY() +
                    (zoneBar.getHeight() / 2f) -
                    (zoneIndicator.getHeight() / 2f));
        });
    }


    private void setPercentColor(int percent) {
        if (!isAdded()) return;

        if (percent >= 80)
            textZonePercent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        else if (percent >= 50)
            textZonePercent.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        else
            textZonePercent.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }


    // ---------------------------------------------------------
    // ADD PEF
    // ---------------------------------------------------------
    private void showAddZoneDialog() {
        boolean pbMissing = (personalBest <= 0);

        if ("child".equalsIgnoreCase(role) && pbMissing) {
            showParentReminderDialog();
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext()).create();

        View v = buildPefDialog(true, dialog);
        dialog.setView(v);

        dialog.show();
    }



    // ---------------------------------------------------------
    // EDIT PEF
    // ---------------------------------------------------------
    private void showEditZoneDialog() {
        boolean pbMissing = (personalBest <= 0);

        if ("child".equalsIgnoreCase(role) && pbMissing) {
            showParentReminderDialog();
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext()).create();

        View v = buildPefDialog(false, dialog);
        dialog.setView(v);

        dialog.show();
    }



    // ---------------------------------------------------------
    // DIALOG BUILDER FOR ADD/EDIT
    // ---------------------------------------------------------
    private View buildPefDialog(boolean isAdd, AlertDialog dialog) {

        View v = getLayoutInflater().inflate(
                isAdd ? R.layout.dialog_add_pef : R.layout.dialog_edit_pef,
                null
        );

        EditText input = v.findViewById(isAdd ? R.id.inputPefValue : R.id.inputEditPef);
        CheckBox cbBefore = v.findViewById(isAdd ? R.id.checkboxBeforeMed : R.id.checkboxBeforeMedEdit);
        CheckBox cbAfter = v.findViewById(isAdd ? R.id.checkboxAfterMed : R.id.checkboxAfterMedEdit);
        Button save = v.findViewById(isAdd ? R.id.btnSavePef : R.id.btnSaveEditPef);

        if (!isAdd) {
            input.setText(textZonePercent.getText().toString().replace("%", ""));
            cbBefore.setChecked(lastBeforeMed);
            cbAfter.setChecked(lastAfterMed);
        }

        cbBefore.setOnCheckedChangeListener((a, checked) -> {
            if (checked) cbAfter.setChecked(false);
        });
        cbAfter.setOnCheckedChangeListener((a, checked) -> {
            if (checked) cbBefore.setChecked(false);
        });

        save.setOnClickListener(btn -> {

            if (input.getText().toString().isEmpty()) {
                input.setError("Enter a value");
                return;
            }
            if (!cbBefore.isChecked() && !cbAfter.isChecked()) {
                Toast.makeText(getContext(), "Select before/after med", Toast.LENGTH_SHORT).show();
                return;
            }

            int pef = Integer.parseInt(input.getText().toString());
            saveZoneWithMedicine(pef, cbBefore.isChecked(), cbAfter.isChecked(), dialog);
        });

        return v;
    }


    // ---------------------------------------------------------
    // SAVE ZONE ENTRY
    // ---------------------------------------------------------
    private void saveZoneWithMedicine(int pef, boolean beforeMed, boolean afterMed, AlertDialog dialog) {

        if (personalBest <= 0 || pef > personalBest) {
            personalBest = pef;
            db.collection("children").document(childId).update("pb", pef);
        }

        int percent = (int)((pef * 100f) / personalBest);

        String zoneColor =
                percent >= 80 ? "GREEN" :
                        percent >= 50 ? "YELLOW" :
                                "RED";

        Map<String, Object> data = new HashMap<>();
        data.put("childId", childId);
        data.put("pef", pef);
        data.put("percent", percent);
        data.put("zone", zoneColor);
        data.put("timestamp", System.currentTimeMillis());
        data.put("beforeMed", beforeMed);
        data.put("afterMed", afterMed);

        db.collection("zone")
                .document()
                .set(data)
                .addOnSuccessListener(unused -> {

                    // 🔵 Update local
                    lastBeforeMed = beforeMed;
                    lastAfterMed = afterMed;
                    updateZoneUI(pef);
                    loadTodayZone();

                    // 🚨🚨🚨 ZONE RED ALERT LOGIC (NEW)
                    if ("RED".equals(zoneColor) && !redAlertShown) {
                        redAlertShown = true;
                        sendParentRedZoneAlert();  // alert for parent
                    }

                    dialog.dismiss();

                });
    }


    private void showLocalRedDialog() {
        if (!isAdded()) return;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Alert")
                .setMessage("Your child's PEF is in the RED zone. Check now.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showParentReminderDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("PB Not Set")
                .setMessage("Ask your parent to set your Personal Best before logging PEF.")
                .setPositiveButton("OK", null)
                .show();
    }
    private void sendParentRedZoneAlert() {

        if (parentId == null) return;

        Map<String, Object> alert = new HashMap<>();
        alert.put("parentId", parentId);
        alert.put("childId", childId);
        alert.put("message", "Your child's PEF is in the RED zone. Check now.");
        alert.put("timestamp", System.currentTimeMillis());
        alert.put("seen", false);
        alert.put("type", "zone");   // optional metadata

        db.collection("alerts").add(alert);
    }

}