package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment that displays a provider's view of a child's profile.
 *
 * <p>This screen is used when a Provider taps on one of their assigned
 * children. It shows the child's name, username, and date of birth pulled
 * from the Firestore "children" collection.</p>
 *
 * <p>Providers can return to their main dashboard using the return button.</p>
 */
public class ProviderProfileFragment extends Fragment {

    /** Displays the child's full name. */
    private TextView childNameView;

    /** Displays the child's username. */
    private TextView childUsernameView;

    /** Displays the child's date of birth. */
    private TextView dobValueView;

    /** Displays the child's avatar (placeholder image). */
    private ImageView childImageView;

    /** Button for navigating back to the Provider main screen. */
    private Button returnButton;

    /** UID of the selected child whose profile is shown. */
    private String childUid;

    /** UID of the logged-in provider viewing this profile. */
    private String providerUid;

    /** Firestore instance used for retrieving child profile data. */
    private FirebaseFirestore db;

    /** Required empty constructor. */
    public ProviderProfileFragment() {}

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater  LayoutInflater for inflating XML
     * @param container Parent container
     * @param savedInstanceState Saved instance state bundle
     * @return Inflated view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_provider_profile, container, false);
    }

    /**
     * Called immediately after the view is created.
     * Initializes UI components, loads Firestore data, and sets up listeners.
     *
     * @param view Root view of the fragment
     * @param savedInstanceState saved instance state bundle
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // ------------------------------------------------
        // Retrieve arguments passed by ProviderMain
        // ------------------------------------------------
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            childUid = extras.getString("childUid");            // Child being viewed
            providerUid = extras.getString("uid"); // Provider viewing the child
        }

        if (childUid == null) {
            Toast.makeText(requireContext(), "Missing child ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // ------------------------------------------------
        // Bind UI elements to XML views
        // ------------------------------------------------
        childNameView = view.findViewById(R.id.providerChildName);
        childUsernameView = view.findViewById(R.id.providerChildUsername);
        dobValueView = view.findViewById(R.id.providerDobValue);
        childImageView = view.findViewById(R.id.profileImageProviderChild);
        returnButton = view.findViewById(R.id.returnToProviderButton);

        // Load Firestore child profile info
        loadChildProfile();

        // ------------------------------------------------
        // Return button: navigate back to ProviderMain
        // ------------------------------------------------
        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ProviderMain.class);
            intent.putExtra("uid", providerUid);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    /**
     * Retrieves the child's profile data from Firestore and updates UI fields.
     *
     * <p>Reads from the "children" collection using the child's UID.</p>
     * <p>On success, the child’s basic information is displayed in the UI.</p>
     * <p>On failure, shows a toast and logs an error.</p>
     */
    private void loadChildProfile() {

        db.collection("children")
                .document(childUid)
                .get()
                .addOnSuccessListener(doc -> {

                    // If child does not exist in Firestore
                    if (!doc.exists()) {
                        Toast.makeText(requireContext(), "Child not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Read fields safely
                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    String username = doc.getString("username");
                    String dob = doc.getString("dob");

                    // Build full name
                    String fullName = ((first != null) ? first : "")
                            + " "
                            + ((last != null) ? last : "");

                    childNameView.setText(fullName.trim().isEmpty() ? "Unknown" : fullName);
                    childUsernameView.setText(username != null ? username : "-");
                    dobValueView.setText(dob != null ? dob : "-");

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to load child profile", Toast.LENGTH_SHORT).show();
                    Log.e("ProviderChildProfile", "Error loading profile", e);
                });
    }
}
