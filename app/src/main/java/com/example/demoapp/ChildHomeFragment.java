package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment representing the home screen shown to a logged-in child user.
 *
 * <p>This screen includes:
 * <ul>
 *     <li>An emergency action button that launches the Triage flow</li>
 *     <li>Optional onboarding popup shown on first visit</li>
 *     <li>A nested ZoneFragment displaying the child's current asthma zone</li>
 *     <li>Automatic alert sent to the parent when triage is opened</li>
 * </ul>
 *
 * <p>The fragment expects two navigation arguments:</p>
 * <ul>
 *     <li><b>"uid"</b> – The child’s Firestore document ID</li>
 *     <li><b>"role"</b> – The user role ("Child") passed from login</li>
 * </ul>
 */
public class ChildHomeFragment extends Fragment {

    /** The Firestore document ID of the current child user. */
    private String childId;

    /** The parent ID associated with this child (loaded from the children collection). */
    private String parentId;

    /** User role passed from login (typically “Child”). */
    private String role;

    /** Firestore instance for database operations. */
    private FirebaseFirestore db;

    /** Ensures the onboarding popup is only shown once per fragment lifecycle. */
    private boolean popupShown = false;

    /** Required empty public constructor. */
    public ChildHomeFragment() {}

    /**
     * Inflates the fragment layout and initializes Firestore.
     *
     * @param inflater Used to inflate the layout.
     * @param container Parent view group.
     * @param savedInstanceState Previous state bundle (unused).
     * @return The inflated UI view.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_children_home, container, false);
        db = FirebaseFirestore.getInstance();

        // Retrieve navigation arguments (child ID + role)
        childId = requireArguments().getString("uid");
        role = requireArguments().getString("role");

        // Example medication button (currently disabled / TODO)
        Button btnLogDose = view.findViewById(R.id.btnLogDose);

        btnLogDose.setOnClickListener(v -> {
            // TODO: Replace with real MedicationActivity when implemented
            // Intent intent = new Intent(getContext(), MedicationActivity.class);
            // intent.putExtra("uid", childId);
            // intent.putExtra("role", role);
            // startActivity(intent);
        });

        return view;
    }

    /**
     * Called immediately after the view hierarchy has been created.
     * Sets up button listeners and loads parent ID from Firestore.
     *
     * @param view The created fragment view.
     * @param savedInstanceState Previously saved state (unused).
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // -----------------------------
        // Emergency Action Button Setup
        // -----------------------------
        View btnEmergencyAction = view.findViewById(R.id.btnResultAction);

        btnEmergencyAction.setOnClickListener(v -> {
            // Notify parent immediately
            sendParentAlertTriageOpenedFromHome();

            // Launch TriageActivity
            Intent i = new Intent(getContext(), TriageActivity.class);
            i.putExtra("uid", childId);
            i.putExtra("role", role);
            startActivity(i);
        });

        // --------------------------------
        // Load parentId from Firestore
        // --------------------------------
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        parentId = doc.getString("parentId");
                    }
                });

        // -------------------------------------
        // Load and inject ZoneFragment (Asthma)
        // -------------------------------------
        if (childId != null) {

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

    /**
     * Called when the Fragment becomes visible again (after returning from another screen).
     * Used to trigger the onboarding popup only once.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (!popupShown && childId != null) {
            popupShown = true;
            checkOnboardingPopup();
        }
    }

    /**
     * Checks whether the onboarding popup has already been shown.
     * If not, displays it and marks the field in Firestore so it will not show again.
     */
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

    /**
     * Displays the onboarding popup dialog for first-time child users.
     * Ensures the fragment is currently attached before showing the dialog.
     */
    private void showOnboardingPopup() {
        if (!isAdded()) return;

        new ChildOnboardingDialog(() -> {})
                .show(getChildFragmentManager(), "childOnboarding");
    }

    /**
     * Sends an alert notification to the parent when the child
     * opens the Triage screen from the home page.
     *
     * <p>This writes an alert document to Firestore containing:</p>
     * <ul>
     *     <li>parentId – who should receive the alert</li>
     *     <li>childId – which child triggered the alert</li>
     *     <li>message – predefined notification message</li>
     *     <li>timestamp – millisecond time</li>
     *     <li>seen – whether the parent has opened the alert</li>
     * </ul>
     */
    private void sendParentAlertTriageOpenedFromHome() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> alert = new HashMap<>();
        alert.put("parentId", parentId);
        alert.put("childId", childId);
        alert.put("message", "Your child is in severe condition Check now.");
        alert.put("timestamp", System.currentTimeMillis());
        alert.put("seen", false);

        db.collection("alerts").add(alert);
    }

}
