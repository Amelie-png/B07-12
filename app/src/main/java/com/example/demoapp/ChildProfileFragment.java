package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment that displays the child user’s profile information.
 *
 * <p>This screen shows:</p>
 * <ul>
 *     <li>Full name</li>
 *     <li>Username</li>
 *     <li>Date of Birth (DOB)</li>
 *     <li>A logout button</li>
 * </ul>
 *
 * <p>The child’s UID must be passed into this fragment via arguments from
 * MainNavActivity (or a parent navigation graph). The fragment retrieves the
 * corresponding Firestore document from the <b>children</b> collection and
 * populates the UI.</p>
 *
 * <p>Logout signs the user out of FirebaseAuth and returns them to SignInActivity.</p>
 */
public class ChildProfileFragment extends Fragment {

    /** TextView displaying the child’s full name. */
    private TextView childName;

    /** TextView displaying the child’s username. */
    private TextView childUsername;

    /** TextView showing the child’s date of birth. */
    private TextView dobValue;

    /** Logout button for ending the session. */
    private Button logoutButton;

    /** Firestore instance for loading child profile data. */
    private FirebaseFirestore db;

    /** The UID of the current child user, passed as a navigation argument. */
    private String childUid;

    /** Required empty public constructor. */
    public ChildProfileFragment() {}

    /**
     * Inflates the fragment’s layout for displaying profile information.
     *
     * @param inflater Used to inflate the fragment’s XML layout.
     * @param container Parent view that holds this fragment.
     * @param savedInstanceState Previously saved UI state (unused here).
     * @return The inflated profile screen layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_profile, container, false);
    }

    /**
     * Called immediately after the layout is created. Binds UI elements,
     * retrieves arguments, loads profile data, and sets up the logout button.
     *
     * @param view The root view of the inflated layout.
     * @param savedInstanceState Previously saved state (unused).
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Retrieve child UID passed from the parent Activity or nav graph
        if (getArguments() != null) {
            childUid = getArguments().getString("uid");
        }

        // Error case: missing UID
        if (childUid == null) {
            Toast.makeText(requireContext(), "Missing child ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bind UI components
        childName = view.findViewById(R.id.childName);
        childUsername = view.findViewById(R.id.childUsername);
        dobValue = view.findViewById(R.id.dobValue);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Load the child's Firestore profile
        loadProfile();

        // Handle logout action
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(requireActivity(), SignInActivity.class);
            startActivity(intent);

            // Close the nav host activity so user cannot return via back button
            requireActivity().finish();
        });
    }

    /**
     * Loads the child's profile data from Firestore and updates the UI accordingly.
     *
     * <p>The following fields are retrieved from the <b>children</b> collection:</p>
     * <ul>
     *     <li>firstName</li>
     *     <li>lastName</li>
     *     <li>username</li>
     *     <li>dob</li>
     * </ul>
     *
     * <p>If the document is missing or retrieval fails, an error message is shown.</p>
     */
    private void loadProfile() {
        db.collection("children")
                .document(childUid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(requireContext(),
                                "Profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    String username = doc.getString("username");
                    String dob = doc.getString("dob");

                    // Build and display full name
                    String fullName = ((first != null) ? first : "") + " "
                            + ((last != null) ? last : "");
                    childName.setText(fullName.trim().isEmpty() ? "Unknown" : fullName);

                    // Username
                    childUsername.setText(username != null ? username : "-");

                    // DOB
                    dobValue.setText(dob != null ? dob : "-");

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to load profile", Toast.LENGTH_SHORT).show();
                    Log.e("ChildProfile", "Error loading child profile", e);
                });
    }
}
