package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demoapp.charts.TrendChartView;
import com.example.demoapp.med.MedicineEntry;
import com.example.demoapp.med.MedicineRepository;
import com.example.demoapp.med.MedicineUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProviderHomeFragment extends Fragment {

    private String childId;
    private String providerUid;
    private FirebaseFirestore db;
    private MedicineRepository repo;

    // Dynamic card views
    private ViewGroup triageContainer;
    private ViewGroup zoneContainer;
    private ViewGroup summaryChartContainer;

    // These only exist when unlocked
    private TextView triageStatus;
    private Button btnTriageHistory;

    // Chart views
    private TrendChartView trendChart;
    private TextView lastRescueTime;
    private TextView weeklyRescueCount;
    private Button btnToggleDays;

    // Chart data
    private List<MedicineEntry> allEntries = new ArrayList<>();
    private int trendDays = 7;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provider_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        repo = new MedicineRepository();

        // -------------------------------------------
        // Read nav-graph arguments
        // -------------------------------------------
        Bundle extras = getActivity().getIntent().getExtras();
        if(extras != null){
            childId = extras.getString("childUid");
            providerUid = extras.getString("uid");
        }

        Log.d("ProviderHomeFragment", "ARG uid = " + childId
                + " | providerUid = " + providerUid
                + " | role = " + requireArguments().getString("role"));

        // -------------------------------------------
        // Bind container layouts
        // -------------------------------------------
        triageContainer = view.findViewById(R.id.triageCardContainer);
        zoneContainer = view.findViewById(R.id.zoneCardContainer);
        summaryChartContainer = view.findViewById(R.id.summaryChartContainer);

        // Load sharing rules
        loadSharingPermissions();
    }

    // -------------------------------------------------------------
    // Load child sharing permissions (triage + PEF + summaryCharts)
    // -------------------------------------------------------------
    private void loadSharingPermissions() {
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    // Read provider binding
                    Map<String, Object> providerBindings =
                            (Map<String, Object>) doc.get("providerBindings");

                    if (providerBindings == null ||
                            !providerBindings.containsKey(providerUid))
                    {
                        showLockedTriage();
                        showLockedZone();
                        showLockedChart();
                        return;
                    }

                    // This provider's binding info
                    String shareCode =
                            (String) providerBindings.get(providerUid);

                    if (shareCode == null) {
                        showLockedTriage();
                        showLockedZone();
                        showLockedChart();
                        return;
                    }

                    // Now read permissions FROM shareCodes → shareCode
                    Map<String, Object> shareCodes =
                            (Map<String, Object>) doc.get("shareCodes");

                    if (shareCodes == null ||
                            !shareCodes.containsKey(shareCode))
                    {
                        showLockedTriage();
                        showLockedZone();
                        showLockedChart();
                        return;
                    }

                    Map<String, Object> shareCodeData =
                            (Map<String, Object>) shareCodes.get(shareCode);

                    Map<String, Object> permissions =
                            (Map<String, Object>) shareCodeData.get("permissions");

                    if (permissions == null) {
                        showLockedTriage();
                        showLockedZone();
                        showLockedChart();
                        return;
                    }

                    boolean canViewPEF =
                            Boolean.TRUE.equals(permissions.get("pef"));
                    boolean canViewTriage =
                            Boolean.TRUE.equals(permissions.get("triageIncidents"));
                    boolean canViewSummaryCharts =
                            Boolean.TRUE.equals(permissions.get("summaryCharts"));

                    Log.d("ProviderHomeFragment", "Permissions - PEF: " + canViewPEF
                            + " | Triage: " + canViewTriage
                            + " | SummaryCharts: " + canViewSummaryCharts);

                    // ---------------- UI APPLY ----------------
                    if (canViewTriage) {
                        showUnlockedTriage();
                        loadLatestTriageState();
                    } else {
                        showLockedTriage();
                    }

                    if (canViewPEF) {
                        loadZoneFragment();
                    } else {
                        showLockedZone();
                    }

                    if (canViewSummaryCharts) {
                        showUnlockedChart();
                        loadMedicineLogsAndShowTrend();
                        getLastRescueTime();
                        getWeeklyRescueCount();
                    } else {
                        showLockedChart();
                    }
                });
    }


    // -------------------------------------------------------------
    // TRIAGE: Locked UI
    // -------------------------------------------------------------
    private void showLockedTriage() {
        triageContainer.removeAllViews();

        View locked = getLayoutInflater()
                .inflate(R.layout.card_provider_triage_locked, triageContainer, false);

        triageContainer.addView(locked);
    }

    // -------------------------------------------------------------
    // TRIAGE: Unlocked UI
    // -------------------------------------------------------------
    private void showUnlockedTriage() {
        triageContainer.removeAllViews();

        View card = getLayoutInflater()
                .inflate(R.layout.card_provider_triage_unlocked, triageContainer, false);

        triageContainer.addView(card);

        // Bind inner views only now (important!)
        triageStatus = card.findViewById(R.id.providerTriageStatus);
        btnTriageHistory = card.findViewById(R.id.btnProviderViewTriageHistory);

        btnTriageHistory.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), TriageHistoryActivity.class);
            i.putExtra("uid", childId);
            startActivity(i);
        });
    }

    // -------------------------------------------------------------
    // ZONE: Locked UI
    // -------------------------------------------------------------
    private void showLockedZone() {
        zoneContainer.removeAllViews();

        View locked = getLayoutInflater()
                .inflate(R.layout.card_provider_zone_locked, zoneContainer, false);

        zoneContainer.addView(locked);
    }

    // -------------------------------------------------------------
    // ZONE: Unlocked UI (real fragment)
    // -------------------------------------------------------------
    private void loadZoneFragment() {
        zoneContainer.removeAllViews();

        ZoneFragment frag = new ZoneFragment();

        Bundle args = new Bundle();
        args.putString("uid", childId);
        args.putString("role", "provider"); // provider = read-only

        frag.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.zoneCardContainer, frag)
                .commit();
    }

    // -------------------------------------------------------------
    // CHART: Locked UI
    // -------------------------------------------------------------
    private void showLockedChart() {
        Log.d("ProviderHomeFragment", "showLockedChart called");

        if (summaryChartContainer == null) {
            Log.e("ProviderHomeFragment", "summaryChartContainer is NULL!");
            return;
        }

        summaryChartContainer.removeAllViews();

        View locked = getLayoutInflater()
                .inflate(R.layout.card_provider_chart_locked, summaryChartContainer, false);

        summaryChartContainer.addView(locked);
        summaryChartContainer.setVisibility(View.VISIBLE);

        Log.d("ProviderHomeFragment", "Locked chart card added");
    }

    // -------------------------------------------------------------
    // CHART: Unlocked UI
    // -------------------------------------------------------------
    private void showUnlockedChart() {
        summaryChartContainer.removeAllViews();

        View chart = getLayoutInflater()
                .inflate(R.layout.card_provider_chart_unlocked, summaryChartContainer, false);

        summaryChartContainer.addView(chart);
        summaryChartContainer.setVisibility(View.VISIBLE);

        // Bind chart views after inflating
        trendChart = chart.findViewById(R.id.trendChart);
        lastRescueTime = chart.findViewById(R.id.tv_last_rescue_time);
        weeklyRescueCount = chart.findViewById(R.id.tv_weekly_rescue_count);
        btnToggleDays = chart.findViewById(R.id.btnToggleDays);

        // Set up toggle button
        if (btnToggleDays != null) {
            btnToggleDays.setText("7 Days → Switch to 30 Days");
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
    }

    // -------------------------------------------------------------
    // TRIAGE: latest status (GREEN/YELLOW/RED)
    // -------------------------------------------------------------
    private void loadLatestTriageState() {
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists() || triageStatus == null) return;

                    String lastState = doc.getString("lastTriageState");
                    if (lastState == null) lastState = "GREEN";

                    triageStatus.setText("Current Status: " + lastState);
                });
    }

    // -------------------------------------------------------------
    // CHART: Load medicine logs and show trend
    // -------------------------------------------------------------
    private void loadMedicineLogsAndShowTrend() {
        long now = System.currentTimeMillis();
        long oneMonthAgo = now - 30L * 24L * 60L * 60L * 1000L;

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
        // Generate trend data
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

        // Call TrendChartView to display
        if (trendChart != null) {
            trendChart.setTrendData(dailyCounts, "Rescue Medicine", trendDays);
        }
    }

    private void getLastRescueTime() {
        repo.fetchLogs(childId, "rescue", 0, Long.MAX_VALUE, new MedicineRepository.OnResult<List<MedicineEntry>>() {
            @Override
            public void onSuccess(List<MedicineEntry> result) {
                long timestamp = MedicineUtils.getLastRescueTime(result);

                if (lastRescueTime != null) {
                    if (timestamp == -1) {
                        lastRescueTime.setText("No rescue logs");
                    } else {
                        lastRescueTime.setText(
                                new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                                        .format(new Date(timestamp))
                        );
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void getWeeklyRescueCount() {
        repo.fetchLogs(childId, "rescue", 0, Long.MAX_VALUE, new MedicineRepository.OnResult<List<MedicineEntry>>() {
            @Override
            public void onSuccess(List<MedicineEntry> result) {
                int count = MedicineUtils.getRescueCountByDay(result, 7);
                if (weeklyRescueCount != null) {
                    weeklyRescueCount.setText(String.valueOf(count));
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }
}