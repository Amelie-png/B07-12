package com.example.demoapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TriageFragment extends Fragment {

    private FirebaseFirestore db;

    // Passed from ChildHomeFragment
    private String childId;
    private String role;
    private String parentId;

    // UI elements
    private CheckBox chkCantSpeak, chkRetractions, chkBlueLips;
    private CheckBox chkCough, chkChestTight;
    private EditText inputRescueAttempts, inputPEF;

    public TriageFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_triage, container, false);

        db = FirebaseFirestore.getInstance();

        // UI bind
        chkCantSpeak = view.findViewById(R.id.chkCantSpeak);
        chkRetractions = view.findViewById(R.id.chkRetractions);
        chkBlueLips = view.findViewById(R.id.chkBlueLips);
        chkCough = view.findViewById(R.id.chkCough);
        chkChestTight = view.findViewById(R.id.chkChestTight);

        inputRescueAttempts = view.findViewById(R.id.inputRescueAttempts);
        inputPEF = view.findViewById(R.id.inputPEF);

        view.findViewById(R.id.btnCheckTriage).setOnClickListener(v -> processTriage());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        childId = args.getString("uid");
        role = args.getString("role");

        if (childId == null) {
            Toast.makeText(getContext(), "Missing child ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(getContext(), "Child record missing", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    parentId = doc.getString("parentId");

                    if (parentId == null) {
                        Toast.makeText(getContext(), "Parent not linked", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // =========================================================================
    // Triage Logic (unchanged)
    // =========================================================================
    private void processTriage() {

        boolean red1 = chkCantSpeak.isChecked();
        boolean red2 = chkRetractions.isChecked();
        boolean red3 = chkBlueLips.isChecked();

        boolean mod1 = chkCough.isChecked();
        boolean mod2 = chkChestTight.isChecked();

        int rescueAttempts = parseIntSafe(inputRescueAttempts.getText().toString());
        int pef = parseIntSafe(inputPEF.getText().toString());

        if (red1 || red2 || red3) {
            sendParentAlert("triage_escalation", "Emergency triggered by red flags.");
            goToEmergencyCard();
            return;
        }

        if (pef > 0 && pef < 50) {
            sendParentAlert("triage_escalation", "PEF < 50% triggered emergency.");
            goToEmergencyCard();
            return;
        }

        if (rescueAttempts >= 3) {
            sendParentAlert("triage_escalation", "Multiple rescue attempts triggered emergency.");
            goToEmergencyCard();
            return;
        }

        if (mod1 || mod2 || (pef > 0 && pef < 80)) {
            goToHomeStepsCard("YELLOW");
            return;
        }

        goToGreenAdviceCard();
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private int parseIntSafe(String s) {
        if (s == null || s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private void sendParentAlert(String type, String message) {
        if (parentId == null) return;

        Map<String, Object> alert = new HashMap<>();
        alert.put("parentId", parentId);
        alert.put("childId", childId);
        alert.put("type", type);
        alert.put("message", message);
        alert.put("timestamp", System.currentTimeMillis());

        db.collection("alerts").document().set(alert);
    }

    private void goToEmergencyCard() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new EmergencyCardFragment())
                .addToBackStack(null)
                .commit();
    }

    private void goToHomeStepsCard(String zone) {
        HomeStepsCardFragment f = HomeStepsCardFragment.newInstance(zone);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
    }

    private void goToGreenAdviceCard() {
        GreenAdviceCardFragment f = new GreenAdviceCardFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
    }
}
