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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ZoneFragment extends Fragment {

    // UI elements
    private LinearLayout layoutNoPEF, layoutHasPEF;
    private TextView textZonePercent;
    private View pefIndicator;
    private Button btnAddPEF, btnEditPEF;

    // Firestore + Data
    private FirebaseFirestore db;
    private String childId;          // REAL childId
    private int personalBest = -1;   // PB not loaded yet

    private ImageView pefBar;


    // -------- REQUIRED for parent → child navigation --------
    public static ZoneFragment newInstance(String childId) {
        ZoneFragment fragment = new ZoneFragment();
        Bundle args = new Bundle();
        args.putString("childId", childId);
        fragment.setArguments(args);
        return fragment;
    }

    public ZoneFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // STEP 1: Determine role (parent or child)
        // (Assuming your users/{uid}/role is implemented by your teammate)
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    String role = doc.getString("role");

                    if ("child".equals(role)) {
                        // Child logs in directly → childId = user's UID
                        childId = user.getUid();

                    } else if ("parent".equals(role)) {
                        // Parent → childId must come from Activity/Navigation
                        if (getArguments() != null) {
                            childId = getArguments().getString("childId");
                        }
                    }

                    if (childId != null) {
                        loadPersonalBest();  // After childId determined
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_zone, container, false);

        // Initialize UI elements
        pefBar = view.findViewById(R.id.pefBar);

        layoutNoPEF = view.findViewById(R.id.layoutNoPEF);
        layoutHasPEF = view.findViewById(R.id.layoutHasPEF);
        textZonePercent = view.findViewById(R.id.textZonePercent);
        pefIndicator = view.findViewById(R.id.pefIndicator);
        btnAddPEF = view.findViewById(R.id.btnAddPEF);
        btnEditPEF = view.findViewById(R.id.btnEditPEF);

        btnAddPEF.setOnClickListener(v -> showAddPefDialog());
        btnEditPEF.setOnClickListener(v -> showEditPefDialog());

        return view;
    }

    // ---------------------------------------------------------
    // LOAD PERSONAL BEST (PB)
    // ---------------------------------------------------------
    private void loadPersonalBest() {
        db.collection("PB")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        personalBest = doc.getLong("value").intValue();
                    } else {
                        personalBest = -1;
                    }

                    // PB loaded → now load today's PEF
                    loadTodayPef();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // ---------------------------------------------------------
    // LOAD TODAY'S PEF
    // ---------------------------------------------------------
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
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private long getTodayStartMillis() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    // ---------------------------------------------------------
    // SAVE PEF + AUTO PB UPDATE
    // ---------------------------------------------------------
    private void savePefToFirestore(int pefValue) {

        // Update PB if needed
        if (personalBest == -1 || pefValue > personalBest) {
            personalBest = pefValue;
            savePersonalBest(pefValue);
        }

        String entryId = db.collection("PEF").document().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("childId", childId);
        data.put("value", pefValue);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("PEF")
                .document(entryId)
                .set(data)
                .addOnSuccessListener(unused -> {
                    updatePefUI(pefValue);
                    saveZoneHistory(pefValue);   // 🔥 ADD THIS
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void savePersonalBest(int value) {
        Map<String, Object> data = new HashMap<>();
        data.put("childId", childId);
        data.put("value", value);

        db.collection("PB")
                .document(childId)
                .set(data)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private String calculateZone(int pefValue) {

        if (personalBest <= 0) return "unknown";

        float percent = (pefValue * 100f) / personalBest;

        if (percent >= 80) {
            return "green";
        } else if (percent >= 50) {
            return "yellow";
        } else {
            return "red";
        }
    }

    // ---------------------------------------------------------
    // UPDATE UI
    // ---------------------------------------------------------
    private void updatePefUI(int pefValue) {
        layoutNoPEF.setVisibility(View.GONE);
        layoutHasPEF.setVisibility(View.VISIBLE);

        if (personalBest > 0) {

            int percent = (int)((pefValue * 100f) / personalBest);
            textZonePercent.setText(percent + "%");

            // Move indicator AFTER layout has been measured
            pefBar.post(() -> {
                int barWidth = pefBar.getWidth();  // actual width of zone_bar
                float x = (percent / 100f) * barWidth;

                pefIndicator.setTranslationX(x - (pefIndicator.getWidth() / 2f));
            });

            // Zone colors
            String zone = calculateZone(pefValue);

            switch (zone) {
                case "green":
                    textZonePercent.setTextColor(
                            getResources().getColor(android.R.color.holo_green_dark)
                    );
                    break;

                case "yellow":
                    textZonePercent.setTextColor(
                            getResources().getColor(android.R.color.holo_orange_dark)
                    );
                    break;

                case "red":
                    textZonePercent.setTextColor(
                            getResources().getColor(android.R.color.holo_red_dark)
                    );
                    break;
            }

        } else {
            textZonePercent.setText(pefValue + " (no PB)");
            pefIndicator.setTranslationX(0);
        }
    }


    private void showNoPefLayout() {
        layoutNoPEF.setVisibility(View.VISIBLE);
        layoutHasPEF.setVisibility(View.GONE);
    }

    // ---------------------------------------------------------
    // ADD + EDIT PEF POPUPS
    // ---------------------------------------------------------
    private void showAddPefDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_pef, null);
        builder.setView(dialogView);

        EditText inputPefValue = dialogView.findViewById(R.id.inputPefValue);
        Button btnSavePef = dialogView.findViewById(R.id.btnSavePef);

        AlertDialog dialog = builder.create();

        btnSavePef.setOnClickListener(v -> {
            String pefText = inputPefValue.getText().toString().trim();

            if (pefText.isEmpty()) {
                inputPefValue.setError("Enter a number");
                return;
            }

            int pefValue = Integer.parseInt(pefText);
            savePefToFirestore(pefValue);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditPefDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_pef, null);
        builder.setView(dialogView);

        EditText inputEditPef = dialogView.findViewById(R.id.inputEditPef);
        Button btnSaveEditPef = dialogView.findViewById(R.id.btnSaveEditPef);

        String currentText = textZonePercent.getText().toString().replace("%", "");
        inputEditPef.setText(currentText);

        AlertDialog dialog = builder.create();

        btnSaveEditPef.setOnClickListener(v -> {
            String pefText = inputEditPef.getText().toString().trim();
            if (pefText.isEmpty()) {
                inputEditPef.setError("Enter a value");
                return;
            }

            int newPef = Integer.parseInt(pefText);
            savePefToFirestore(newPef);
            dialog.dismiss();
        });

        dialog.show();
    }
    private void saveZoneHistory(int pefValue) {

        if (personalBest <= 0) return;  // can't compute zone

        int percent = (int)((pefValue * 100f) / personalBest);
        String zone = calculateZone(pefValue);

        String historyId = db.collection("ZoneHistory").document().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("childId", childId);
        data.put("pefValue", pefValue);
        data.put("percent", percent);
        data.put("zone", zone);
        data.put("timestamp", System.currentTimeMillis());
        data.put("date", getTodayDateString()); // readable date

        db.collection("ZoneHistory")
                .document(historyId)
                .set(data)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Zone history failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private String getTodayDateString() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new java.util.Date());
    }

}
