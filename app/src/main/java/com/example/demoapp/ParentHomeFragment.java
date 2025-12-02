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

import com.example.demoapp.charts.TrendChartView;
import com.example.demoapp.med.MedicineEntry;
import com.example.demoapp.med.MedicineRepository;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ParentHomeFragment extends Fragment {

    private String childId;
    private String parentId;
    private FirebaseFirestore db;

    private TextView emergencyText;
    private Button btnViewTriageHistory;
    private TrendChartView trendChart;
    private Button btnToggleDays;

    private ListenerRegistration alertListener;
    private List<MedicineEntry> allEntries = new ArrayList<>();

    private int trendDays = 7; // 默认 7 天

    public ParentHomeFragment() { }

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
            parentId = getArguments().getString("parentId");
        }

        if (childId == null) {
            Toast.makeText(requireContext(), "Child ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

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
        loadMedicineLogsAndShowTrend();
    }

    private void bindUI(View view) {
        emergencyText = view.findViewById(R.id.emergencyText);
        btnViewTriageHistory = view.findViewById(R.id.btnViewTriageHistory);
        trendChart = view.findViewById(R.id.trendChart);
        btnToggleDays = view.findViewById(R.id.btnToggleDays);

        // 初始化按钮文字
        btnToggleDays.setText("7 Days → Switch to 30 Days");

        btnViewTriageHistory.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), TriageHistoryActivity.class);
            i.putExtra("uid", childId);
            startActivity(i);
        });

        btnToggleDays.setOnClickListener(v -> {
            if (trendDays == 7) {
                trendDays = 30;
                btnToggleDays.setText("30 Days → Switch to 7 Days");
            } else {
                trendDays = 7;
                btnToggleDays.setText("7 Days → Switch to 30 Days");
            }
            updateTrendChart();
        });
    }


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

    private void startListeningForAlerts() {
        if (parentId == null) return;

        alertListener = db.collection("alerts")
                .whereEqualTo("parentId", parentId)
                .whereEqualTo("seen", false)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    for (DocumentChange change : snap.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            String message = change.getDocument().getString("message");
                            String alertId = change.getDocument().getId();
                            showAlertPopup(message);
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

    private void loadMedicineLogsAndShowTrend() {
        MedicineRepository repo = new MedicineRepository();
        long now = System.currentTimeMillis();
        long oneMonthAgo = now - 30L * 24L * 60L * 60L * 1000L; // 拉取 30 天数据

        repo.fetchLogs(childId, null, oneMonthAgo, now, new MedicineRepository.OnResult<List<MedicineEntry>>() {
            @Override
            public void onSuccess(List<MedicineEntry> result) {
                allEntries.clear();
                allEntries.addAll(result);
                updateTrendChart();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateTrendChart() {
        // 生成 trend 数据
        List<Float> dailyCounts = new ArrayList<>();
        for (int i = 0; i < trendDays; i++) dailyCounts.add(0f);

        long now = System.currentTimeMillis();

        for (MedicineEntry entry : allEntries) {
            if ("rescue".equals(entry.getMedType())) {
                long diffDays = (now - entry.getTimestampValue()) / (24L * 60L * 60L * 1000L);
                int index = (int) (trendDays - 1 - diffDays);
                if (index >= 0 && index < trendDays) {
                    dailyCounts.set(index, dailyCounts.get(index) + 1f);
                }
            }
        }

        // 调用 TrendChartView 显示
        trendChart.setTrendData(dailyCounts, "Rescue Medicine", trendDays);
    }
}
