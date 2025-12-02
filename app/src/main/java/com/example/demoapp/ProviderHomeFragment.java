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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ProviderHomeFragment extends Fragment {

    private String childId;
    private String providerUid;
    private FirebaseFirestore db;

    // Dynamic card views
    private ViewGroup triageContainer;
    private ViewGroup zoneContainer;

    // These only exist when unlocked
    private TextView triageStatus;
    private Button btnTriageHistory;

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

        // -------------------------------------------
        // Read nav-graph arguments
        // -------------------------------------------
        childId = requireArguments().getString("uid");
        providerUid = requireArguments().getString("providerUid");

        Log.d("ProviderHomeFragment", "ARG uid = " + childId
                + " | providerUid = " + providerUid
                + " | role = " + requireArguments().getString("role"));

        // -------------------------------------------
        // Bind container layouts
        // -------------------------------------------
        triageContainer = view.findViewById(R.id.triageCardContainer);
        zoneContainer = view.findViewById(R.id.zoneCardContainer);

        // Load sharing rules
        loadSharingPermissions();
    }

    // -------------------------------------------------------------
    // Load child sharing permissions (triage + PEF)
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
                        return;
                    }

                    // This provider's binding info
                    Map<String, Object> binding =
                            (Map<String, Object>) providerBindings.get(providerUid);

                    String shareCode = (String) binding.get("activeShareCode");

                    if (shareCode == null) {
                        showLockedTriage();
                        showLockedZone();
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
                        return;
                    }

                    Map<String, Object> shareCodeData =
                            (Map<String, Object>) shareCodes.get(shareCode);

                    Map<String, Object> permissions =
                            (Map<String, Object>) shareCodeData.get("permissions");

                    if (permissions == null) {
                        showLockedTriage();
                        showLockedZone();
                        return;
                    }

                    boolean canViewPEF =
                            Boolean.TRUE.equals(permissions.get("pef"));
                    boolean canViewTriage =
                            Boolean.TRUE.equals(permissions.get("triageIncidents"));


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
}
