package com.example.demoapp;

import android.app.AlertDialog;
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
    private LinearLayout layoutNoPEF, layoutHasPEF;
    private TextView textZonePercent;
    private View pefIndicator;
    private Button btnAddPEF, btnEditPEF;
    private ImageView pefBar;

    // Firestore
    private FirebaseFirestore db;

    // Passed from HomeFragment
    private String childId;   // child being displayed
    private String parentId;

    private String role;      // "child", "parent", "provider"

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
        pefBar = view.findViewById(R.id.pefBar);
        layoutNoPEF = view.findViewById(R.id.layoutNoPEF);
        layoutHasPEF = view.findViewById(R.id.layoutHasPEF);
        textZonePercent = view.findViewById(R.id.textZonePercent);
        pefIndicator = view.findViewById(R.id.pefIndicator);
        btnAddPEF = view.findViewById(R.id.btnAddPEF);
        btnEditPEF = view.findViewById(R.id.btnEditPEF);

        // Events
        btnAddPEF.setOnClickListener(v -> showAddPefDialog());
        btnEditPEF.setOnClickListener(v -> showEditPefDialog());

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        childId = args.getString("uid");
        role = args.getString("role");

        // ⭐ 导入 parentId
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
    // Load PB + PEF
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

                    loadTodayPef();
                });
    }


    private void loadTodayPef() {

        long todayStart = getTodayStartMillis();

        db.collection("PEF")
                .whereEqualTo("childId", childId)
                .whereGreaterThan("timestamp", todayStart)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        showNoPefLayout();
                    } else {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        int value = doc.getLong("value").intValue();
                        updatePefUI(value);
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
    // Update UI + Moving Indicator
    // =========================================================================
    private void updatePefUI(int pefValue) {

        layoutNoPEF.setVisibility(View.GONE);
        layoutHasPEF.setVisibility(View.VISIBLE);

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

        // ------------------------------------------------------
        // ALWAYS send alert if RED zone today
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

        pefBar.post(() -> {

            float barWidth = pefBar.getWidth();
            float px = (percent / 100f) * barWidth;

            // Center the dot (so it doesn't align left)
            float centerX = px - (pefIndicator.getWidth() / 2f);

            pefIndicator.setX(centerX);
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

    private void showNoPefLayout() {
        layoutNoPEF.setVisibility(View.VISIBLE);
        layoutHasPEF.setVisibility(View.GONE);
    }


    // =========================================================================
    // Add PEF Dialog
    // =========================================================================
    private void showAddPefDialog() {

        // Only providers cannot edit
        if ("provider".equals(role)) {
            Toast.makeText(getContext(), "Providers cannot edit PEF", Toast.LENGTH_SHORT).show();
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

            savePef(value);
            dialog.dismiss();
        });

        dialog.show();
    }


    // =========================================================================
    // Edit PEF Dialog
    // =========================================================================
    private void showEditPefDialog() {

        if ("provider".equals(role)) {
            Toast.makeText(getContext(), "Providers cannot edit PEF", Toast.LENGTH_SHORT).show();
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

            savePef(value);
            dialog.dismiss();
        });

        dialog.show();
    }


    // =========================================================================
    // Save PEF
    // =========================================================================
    private void savePef(int pefValue) {

        // Update PB if needed
        if (personalBest == -1 || pefValue > personalBest) {
            personalBest = pefValue;
            savePersonalBest(pefValue);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("childId", childId);
        data.put("value", pefValue);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("PEF")
                .document()
                .set(data)
                .addOnSuccessListener(unused -> updatePefUI(pefValue));
    }

    private void savePersonalBest(int value) {
        db.collection("children")
                .document(childId)
                .update("pb", value)
                .addOnFailureListener(e -> {
                    // If the document doesn't exist, fallback to set()
                    Map<String, Object> data = new HashMap<>();
                    data.put("pb", value);

                    db.collection("children")
                            .document(childId)
                            .set(data);
                });
    }

}
