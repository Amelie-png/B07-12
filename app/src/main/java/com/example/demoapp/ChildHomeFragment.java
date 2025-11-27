package com.example.demoapp;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChildHomeFragment extends Fragment {

    private String childId;
    private String role;
    private FirebaseFirestore db;
    private boolean popupShown = false;

    public ChildHomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_children_home, container, false);
        db = FirebaseFirestore.getInstance();

        // Retrieve child ID & role from Navigation args
        childId = requireArguments().getString("uid");
        role = requireArguments().getString("role");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (childId != null) {

            // Pass both childId and role into ZoneFragment
            Bundle args = new Bundle();
            args.putString("uid", childId);
            args.putString("role", role);

            ZoneFragment zoneFragment = new ZoneFragment();
            zoneFragment.setArguments(args);

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.zoneFragmentContainer, zoneFragment)
                    .commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!popupShown && childId != null) {
            popupShown = true;
            checkOnboardingPopup();
        }
    }

    private void checkOnboardingPopup() {
        DocumentReference childDoc = db.collection("children").document(childId);

        childDoc.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) return;

            Boolean hasSeen = snapshot.getBoolean("hasSeenOnboardingChild");

            if (hasSeen == null || !hasSeen) {
                showOnboardingPopup();
                childDoc.update("hasSeenOnboardingChild", true);
            }
        });
    }

    private void showOnboardingPopup() {
        if (!isAdded()) return;

        new ChildOnboardingDialog(() -> {})
                .show(getChildFragmentManager(), "childOnboarding");
    }
}
