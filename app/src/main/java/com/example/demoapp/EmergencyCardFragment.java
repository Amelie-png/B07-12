package com.example.demoapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EmergencyCardFragment extends Fragment {

    public EmergencyCardFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.card_emergency, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button backHomeBtn = view.findViewById(R.id.btnBackHome);




        // Back to home
        backHomeBtn.setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );
    }
    private void sendParentAlert(String type, String message) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> alert = new HashMap<>();
        alert.put("childId", childId);
        alert.put("parentId", parentId);  // you must pass this in from parent
        alert.put("type", type);          // triage_start / triage_escalation
        alert.put("message", message);
        alert.put("timestamp", System.currentTimeMillis());

        db.collection("alerts")
                .add(alert)
                .addOnSuccessListener(doc -> {
                    System.out.println("Parent alert saved.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Alert failed: " + e.getMessage());
                });
    }

}
