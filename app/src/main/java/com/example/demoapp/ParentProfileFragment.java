package com.example.demoapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment used by parents to view their child's profile information
 * and edit the child's Personal Best (PB) PEF value.
 *
 * <p>This screen allows a parent to:
 * <ul>
 *     <li>View the child’s name, username, DOB, and PB</li>
 *     <li>Edit the Personal Best (PB) value</li>
 *     <li>Return to the parent home screen</li>
 * </ul>
 *
 * All data is retrieved from the "children" collection in Firestore.
 */
public class ParentProfileFragment extends Fragment {

    /** Displays the child's full name. */
    private TextView childName;

    /** Displays the child's username. */
    private TextView childUsername;

    /** Displays the child’s date of birth. */
    private TextView dobValue;

    /** Displays the child's personal best (PB) reading. */
    private TextView pbValue;

    /** Button that opens the PB editing dialog. */
    private Button editPBButton;

    /** Button that navigates the parent back to ParentActivity. */
    private Button returnButton;

    /** The unique ID of the child whose profile is displayed. */
    private String childUid;

    /** The parent’s UID (used to return to their home screen). */
    private String parentUid;

    /** Firestore instance used for loading and updating child data. */
    private FirebaseFirestore db;

    /** Required empty constructor. */
    public ParentProfileFragment() {}

    /**
     * Inflates the fragment layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_profile, container, false);
    }

    /**
     * Called after the view is created; initializes UI components,
     * retrieves arguments, loads child data, and sets button listeners.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // -----------------------------
        // Retrieve child + parent IDs
        // -----------------------------
        if (getArguments() != null) {
            childUid = getArguments().getString("uid");
            parentUid = getArguments().getString("parentUid");
        }

        if (childUid == null) {
            Toast.makeText(requireContext(), "Missing child ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // -----------------------------
        // Bind UI elements
        // -----------------------------
        childName = view.findViewById(R.id.parentChildName);
        childUsername = view.findViewById(R.id.parentChildUsername);
        dobValue = view.findViewById(R.id.parentDobValue);
        pbValue = view.findViewById(R.id.parentPBValue);
        editPBButton = view.findViewById(R.id.parentEditPBButton);
        returnButton = view.findViewById(R.id.returnToParentButton);

        // Load child profile details
        loadProfile();

        // Open PB editing dialog
        editPBButton.setOnClickListener(v -> showPBDialog());

        // Return to parent home screen
        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ParentActivity.class);
            intent.putExtra("uid", parentUid);
            intent.putExtra("role", "parent");
            startActivity(intent);
            requireActivity().finish();
        });
    }

    /**
     * Loads the child's profile information from Firestore
     * and populates the UI fields.
     */
    private void loadProfile() {
        db.collection("children")
                .document(childUid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(requireContext(), "Child not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Extract fields
                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    String username = doc.getString("username");
                    String dob = doc.getString("dob");
                    Double pb = doc.getDouble("pb");

                    // Display full name
                    String fullName =
                            (first != null ? first : "") + " " +
                                    (last != null ? last : "");
                    childName.setText(fullName.trim());

                    // Username
                    childUsername.setText(username != null ? username : "-");

                    // DOB
                    dobValue.setText(dob != null ? dob : "-");

                    // PB
                    pbValue.setText(pb != null ? String.valueOf(pb) : "0");

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load child data", Toast.LENGTH_SHORT).show();
                    Log.e("ParentProfile", "Error loading child profile", e);
                });
    }

    /**
     * Displays an AlertDialog allowing the parent to update the
     * child's PB (Personal Best) value.
     */
    private void showPBDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Personal Best");

        final EditText input = new EditText(requireContext());
        input.setHint("Enter new PB");
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        builder.setView(input);

        // Save PB
        builder.setPositiveButton("Save", (dialog, which) -> {

            String value = input.getText().toString().trim();

            if (value.isEmpty()) {
                Toast.makeText(requireContext(), "PB cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            double newPB;

            // Validate numeric PB
            try {
                newPB = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                return;
            }

            savePB(newPB);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Updates the Personal Best (PB) value in Firestore and refreshes the UI.
     *
     * @param newPB The new PB value entered by the parent.
     */
    private void savePB(double newPB) {
        db.collection("children")
                .document(childUid)
                .update("pb", newPB)
                .addOnSuccessListener(aVoid -> {

                    // Update UI
                    pbValue.setText(String.valueOf(newPB));
                    Toast.makeText(requireContext(), "PB updated!", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to update PB", Toast.LENGTH_SHORT).show()
                );
    }
}
