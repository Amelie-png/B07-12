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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ZoneFragment extends Fragment {

    private LinearLayout layoutNoZone, layoutHasZone, layoutNoPB;
    private TextView textZonePercent;
    private View zoneIndicator;
    private Button btnAddZone, btnEditZone;
    private ImageView zoneBar;

    private FirebaseFirestore db;
    private String childId;
    private String parentId;
    private String role;

    private int personalBest = -1;

    public ZoneFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_zone, container, false);

        db = FirebaseFirestore.getInstance();

        zoneBar = view.findViewById(R.id.pefBar);
        layoutNoZone = view.findViewById(R.id.layoutNoPEF);
        layoutHasZone = view.findViewById(R.id.layoutHasPEF);
        layoutNoPB = view.findViewById(R.id.layoutNoPB);

        textZonePercent = view.findViewById(R.id.textZonePercent);
        zoneIndicator = view.findViewById(R.id.pefIndicator);
        btnAddZone = view.findViewById(R.id.btnAddPEF);
        btnEditZone = view.findViewById(R.id.btnEditPEF);

        Button btnZoneHistory = view.findViewById(R.id.btnZoneHistory);
        btnZoneHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ZoneHistoryActivity.class);
            intent.putExtra("uid", childId);
            startActivity(intent);
        });

        btnAddZone.setOnClickListener(v -> showAddZoneDialog());
        btnEditZone.setOnClickListener(v -> showEditZoneDialog());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        childId = args.getString("uid");
        role = args.getString("role");

        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        parentId = doc.getString("parentId");
                    }
                });

        loadPersonalBest();
    }


    // ----------------------------------------------------
    // LOAD PERSONAL BEST
    // ----------------------------------------------------
    private void loadPersonalBest() {
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists() && doc.contains("pb")) {
                        personalBest = doc.getLong("pb").intValue();
                    }

                    boolean pbNotSet = (personalBest <= 0);

                    // CHILD + PB NOT SET → Show special layout and STOP
                    if (role != null && role.equalsIgnoreCase("child") && pbNotSet) {
                        showNoPBLayout();
                        return;
                    }

                    updateButtonStates();
                    loadTodayZone();
                });
    }


    // ----------------------------------------------------
    // SPECIAL LAYOUT WHEN PB NOT SET (CHILD ONLY)
    // ----------------------------------------------------
    private void showNoPBLayout() {
        layoutNoPB.setVisibility(View.VISIBLE);
        layoutNoZone.setVisibility(View.GONE);
        layoutHasZone.setVisibility(View.GONE);

        btnAddZone.setVisibility(View.GONE);
        btnEditZone.setVisibility(View.GONE);
    }


    // ----------------------------------------------------
    // ENABLE / DISABLE PEF BUTTONS BASED ON PB
    // ----------------------------------------------------
    private void updateButtonStates() {
        if (role != null && role.equalsIgnoreCase("child")) {

            boolean pbNotSet = (personalBest <= 0);

            btnAddZone.setEnabled(!pbNotSet);
            btnEditZone.setEnabled(!pbNotSet);

            float alpha = pbNotSet ? 0.4f : 1f;
            btnAddZone.setAlpha(alpha);
            btnEditZone.setAlpha(alpha);

            if (pbNotSet) {
                btnAddZone.setVisibility(View.GONE);
                btnEditZone.setVisibility(View.GONE);
            }
        }
    }


    // ----------------------------------------------------
    // LOAD TODAY’S ZONE DATA
    // ----------------------------------------------------
    private void loadTodayZone() {
        long todayStart = getTodayStartMillis();

        db.collection("zone")
                .whereEqualTo("childId", childId)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        showNoZoneLayout();
                        return;
                    }

                    long latestTimestamp = -1;
                    DocumentSnapshot latestDoc = null;

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Long ts = doc.getLong("timestamp");
                        if (ts == null) continue;

                        if (ts >= todayStart && ts > latestTimestamp) {
                            latestTimestamp = ts;
                            latestDoc = doc;
                        }
                    }

                    if (latestDoc == null) {
                        showNoZoneLayout();
                        return;
                    }

                    int pefValue = latestDoc.getLong("pef").intValue();
                    updateZoneUI(pefValue);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    showNoZoneLayout();
                });
    }


    private long getTodayStartMillis() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }


    // ----------------------------------------------------
    // SHOW NO-ZONE LAYOUT
    // ----------------------------------------------------
    private void showNoZoneLayout() {
        layoutNoZone.setVisibility(View.VISIBLE);
        layoutHasZone.setVisibility(View.GONE);
        layoutNoPB.setVisibility(View.GONE);
    }


    // ----------------------------------------------------
    // UPDATE UI FOR TODAY'S ZONE
    // ----------------------------------------------------
    private void updateZoneUI(int pefValue) {

        layoutNoZone.setVisibility(View.GONE);
        layoutHasZone.setVisibility(View.VISIBLE);
        layoutNoPB.setVisibility(View.GONE);

        int percent = personalBest > 0
                ? (int) ((pefValue * 100f) / personalBest)
                : -1;

        if (percent >= 0) {
            textZonePercent.setText(percent + "%");
            moveIndicator(percent);
            setPercentColor(percent);
        } else {
            textZonePercent.setText(pefValue + " (no PB)");
        }
    }


    private void moveIndicator(int percent) {
        zoneBar.post(() -> {
            float barWidth = zoneBar.getWidth();
            float px = percent / 100f * barWidth;
            zoneIndicator.setX(px - zoneIndicator.getWidth() / 2f);
        });
    }


    private void setPercentColor(int percent) {
        if (percent >= 80) {
            textZonePercent.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (percent >= 50) {
            textZonePercent.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            textZonePercent.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }


    // ----------------------------------------------------
    // PB REMINDER DIALOG
    // ----------------------------------------------------
    private void showParentReminderDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Personal Best Not Set")
                .setMessage("Please remind your parent to set your Personal Best (PB) before logging PEF.")
                .setPositiveButton("OK", null)
                .show();
    }


    // ----------------------------------------------------
    // ADD PEF DIALOG
    // ----------------------------------------------------
    private void showAddZoneDialog() {

        boolean pbNotSet = (personalBest <= 0);

        if (role != null && role.equalsIgnoreCase("child") && pbNotSet) {
            showParentReminderDialog();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_pef, null);
        builder.setView(dialogView);

        EditText input = dialogView.findViewById(R.id.inputPefValue);
        Button save = dialogView.findViewById(R.id.btnSavePef);

        AlertDialog dialog = builder.create();

        save.setOnClickListener(v -> {
            if (input.getText().toString().isEmpty()) {
                input.setError("Enter a value");
                return;
            }

            saveZone(Integer.parseInt(input.getText().toString()));
            dialog.dismiss();
        });

        dialog.show();
    }


    // ----------------------------------------------------
    // EDIT PEF DIALOG
    // ----------------------------------------------------
    private void showEditZoneDialog() {

        boolean pbNotSet = (personalBest <= 0);

        if (role != null && role.equalsIgnoreCase("child") && pbNotSet) {
            showParentReminderDialog();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_pef, null);
        builder.setView(dialogView);

        EditText input = dialogView.findViewById(R.id.inputEditPef);
        Button save = dialogView.findViewById(R.id.btnSaveEditPef);

        input.setText(textZonePercent.getText().toString().replace("%", ""));

        AlertDialog dialog = builder.create();

        save.setOnClickListener(v -> {
            if (input.getText().toString().isEmpty()) {
                input.setError("Enter a value");
                return;
            }

            saveZone(Integer.parseInt(input.getText().toString()));
            dialog.dismiss();
        });

        dialog.show();
    }


    // ----------------------------------------------------
    // SAVE ZONE ENTRY
    // ----------------------------------------------------
    private void saveZone(int pefValue) {

        if (personalBest <= 0 || pefValue > personalBest) {
            personalBest = pefValue;
            db.collection("children").document(childId).update("pb", pefValue);
            updateButtonStates();
        }

        int percent = (int)((pefValue * 100f) / personalBest);

        String zoneColor = percent >= 80 ? "GREEN" :
                percent >= 50 ? "YELLOW" :
                        "RED";

        Map<String, Object> data = new HashMap<>();
        data.put("childId", childId);
        data.put("pef", pefValue);
        data.put("percent", percent);
        data.put("zone", zoneColor);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("zone")
                .document()
                .set(data)
                .addOnSuccessListener(unused -> {
                    updateZoneUI(pefValue);
                    loadTodayZone();
                });
    }
}
