package com.example.demoapp;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;

public class ProviderHomeFragment extends Fragment {

    private String childId;
    private FirebaseFirestore db;

    private View triageCard;
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
        childId = requireArguments().getString("uid");

        triageCard = view.findViewById(R.id.triageCard);
        triageStatus = view.findViewById(R.id.providerTriageStatus);
        btnTriageHistory = view.findViewById(R.id.btnProviderViewTriageHistory);

        btnTriageHistory.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), TriageHistoryActivity.class);
            i.putExtra("uid", childId);
            startActivity(i);
        });

        loadSharingPermissions(view);
    }

    private void loadSharingPermissions(View view) {
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    // Read sharing object
                    Map<String, Object> sharing =
                            (Map<String, Object>) doc.get("sharing");

                    if (sharing == null) return;

                    boolean canViewPEF = Boolean.TRUE.equals(sharing.get("pef"));
                    boolean canViewTriage = Boolean.TRUE.equals(sharing.get("triageIncidents"));

                    // Apply permissions
                    triageCard.setVisibility(canViewTriage ? View.VISIBLE : View.GONE);

                    if (canViewPEF) loadZoneFragment();
                    else view.findViewById(R.id.zoneFragmentContainer).setVisibility(View.GONE);

                    if (canViewTriage) loadLatestTriageState();
                });
    }

    private void loadLatestTriageState() {
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String lastState = doc.getString("lastTriageState");
                    if (lastState == null) lastState = "GREEN";

                    triageStatus.setText("Current Status: " + lastState);
                });
    }

    private void loadZoneFragment() {
        ZoneFragment frag = new ZoneFragment();

        Bundle args = new Bundle();
        args.putString("uid", childId);
        args.putString("role", "provider"); // provider = read-only
        frag.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.zoneFragmentContainer, frag)
                .commit();
    }
}