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
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ZoneFragment extends Fragment {

    // UI elements
    private LinearLayout layoutNoZone, layoutHasZone;
    private TextView textZonePercent;
    private View zoneIndicator;
    private Button btnAddZone, btnEditZone;
    private ImageView zoneBar;

    // Firestore
    private FirebaseFirestore db;

    // Passed from HomeFragment
    private String childId;
    private String parentId;
    private String role;  // "child", "parent", "provider"

    private int personalBest = -1;

    public ZoneFragment() {}


    // =========================================================================
    // Lifecycle
    // =========================================================================
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_zone, container, false);

        db = FirebaseFirestore.getInstance();

        // Initialize UI
        zoneBar = view.findViewById(R.id.pefBar);
        layoutNoZone = view.findViewById(R.id.layoutNoPEF);
        layoutHasZone = view.findViewById(R.id.layoutHasPEF);
        textZonePercent = view.findViewById(R.id.textZonePercent);
        zoneIndicator = view.findViewById(R.id.pefIndicator);
        btnAddZone = view.findViewById(R.id.btnAddPEF);
        btnEditZone = view.findViewById(R.id.btnEditPEF);

        // History button
        Button btnZoneHistory = view.findViewById(R.id.btnZoneHistory);
        btnZoneHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ZoneHistoryActivity.class);
            intent.putExtra("uid", childId);
            startActivity(intent);
        });

        // Add / Edit zone
        btnAddZone.setOnClickListener(v -> showAddZoneDialog());
        btnEditZone.setOnClickListener(v -> showEditZoneDialog());

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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

        if (childId != null) {
            loadPersonalBest();
        }
    }



    // =========================================================================
    // Load PB + today's ZONE
    // =========================================================================
    private void loadPersonalBest() {
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("pb")) {
                        personalBest = doc.getLong("pb").intValue();
                    } else {
                        personalBest = -1;
                    }
                    loadTodayZone();
                });
    }


    private void loadTodayZone() {

        long todayStart = getTodayStartMillis();

        db.collection("zone")
                .whereEqualTo("childId", childId)
                .whereGreaterThan("timestamp", todayStart)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        showNoZoneLayout();
                    } else {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        int pef = doc.getLong("pef").intValue();
                        updateZoneUI(pef);
                    }
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



    // =========================================================================
    // Update UI
    // =========================================================================
    private void updateZoneUI(int pefValue) {

        layoutNoZone.setVisibility(View.GONE);
        layoutHasZone.setVisibility(View.VISIBLE);

        int percent = (personalBest > 0)
                ? (int)((pefValue * 100f) / personalBest)
                : -1;

        if (percent >= 0) {
            textZonePercent.setText(percent + "%");
            moveIndicator(percent);
            setPercentColor(percent);
        } else {
            textZonePercent.setText(pefValue + " (no PB)");
        }

        // Send RED alert to parent
        if (percent > 0 && percent < 50) {
            sendParentAlertDailyRed();
        }
    }


    private void sendParentAlertDailyRed() {
        if (parentId == null) return;

        Map<String, Object> alert = new HashMap<>();
        alert.put("parentId", parentId);
        alert.put("childId", childId);
        alert.put("type", "daily_zone_red");
        alert.put("message", "Today's PEF zone is RED (<50% of PB).");
        alert.put("timestamp", System.currentTimeMillis());

        db.collection("alerts").document().set(alert);
    }



    private void moveIndicator(int percent) {

        zoneBar.post(() -> {

            float barWidth = zoneBar.getWidth();
            float px = (percent / 100f) * barWidth;

            float centerX = px - (zoneIndicator.getWidth() / 2f);
            zoneIndicator.setX(centerX);
        });
    }

    private void setPercentColor(int percent) {
        if (percent >= 80) {
            textZonePercent.setTextColor(
                    getResources().getColor(android.R.color.holo_green_dark));
        } else if (percent >= 50) {
            textZonePercent.setTextColor(
                    getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            textZonePercent.setTextColor(
                    getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void showNoZoneLayout() {
        layoutNoZone.setVisibility(View.VISIBLE);
        layoutHasZone.setVisibility(View.GONE);
    }



    // =========================================================================
    // Add Zone Dialog
    // =========================================================================
    private void showAddZoneDialog() {

        if ("provider".equals(role)) {
            Toast.makeText(getContext(), "Providers cannot edit", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_add_pef, null);
        builder.setView(view);

        EditText input = view.findViewById(R.id.inputPefValue);
        Button save = view.findViewById(R.id.btnSavePef);

        AlertDialog dialog = builder.create();

        save.setOnClickListener(v -> {
            String s = input.getText().toString();
            if (s.isEmpty()) {
                input.setError("Enter a value");
                return;
            }
            int value = Integer.parseInt(s);

            saveZone(value);
            dialog.dismiss();
        });

        dialog.show();
    }


    // =========================================================================
    // Edit Zone Dialog
    // =========================================================================
    private void showEditZoneDialog() {

        if ("provider".equals(role)) {
            Toast.makeText(getContext(), "Providers cannot edit", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_pef, null);
        builder.setView(view);

        EditText input = view.findViewById(R.id.inputEditPef);
        Button save = view.findViewById(R.id.btnSaveEditPef);

        String current = textZonePercent.getText().toString().replace("%", "");
        input.setText(current);

        AlertDialog dialog = builder.create();

        save.setOnClickListener(v -> {
            String s = input.getText().toString();
            if (s.isEmpty()) {
                input.setError("Enter a value");
                return;
            }

            int value = Integer.parseInt(s);

            saveZone(value);
            dialog.dismiss();
        });

        dialog.show();
    }



    // =========================================================================
    // Save Zone
    // =========================================================================
    private void saveZone(int pefValue) {

        // Update PB if needed
        if (personalBest == -1 || pefValue > personalBest) {
            personalBest = pefValue;
            savePersonalBest(pefValue);
        }

        int percent = (int)((pefValue * 100f) / personalBest);
        String zoneColor =
                percent >= 80 ? "GREEN" :
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
                .addOnSuccessListener(unused -> updateZoneUI(pefValue));
    }


    private void savePersonalBest(int value) {
        db.collection("children")
                .document(childId)
                .update("pb", value)
                .addOnFailureListener(e -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("pb", value);

                    db.collection("children")
                            .document(childId)
                            .set(data);
                });
    }

}