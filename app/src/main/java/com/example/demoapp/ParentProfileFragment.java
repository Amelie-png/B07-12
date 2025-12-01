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

public class ParentProfileFragment extends Fragment {

    private TextView childName;
    private TextView childUsername;
    private TextView dobValue;
    private TextView pbValue;
    private Button editPBButton;
    private Button returnButton;

    private String childUid;
    private String parentUid;

    private FirebaseFirestore db;

    public ParentProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Retrieve both IDs
        if (getArguments() != null) {
            childUid = getArguments().getString("uid");         // Child ID
            parentUid = getArguments().getString("parentUid");  // Parent ID (optional)
        }

        if (childUid == null) {
            Toast.makeText(requireContext(), "Missing child ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bind UI
        childName = view.findViewById(R.id.parentChildName);
        childUsername = view.findViewById(R.id.parentChildUsername);
        dobValue = view.findViewById(R.id.parentDobValue);
        pbValue = view.findViewById(R.id.parentPBValue);
        editPBButton = view.findViewById(R.id.parentEditPBButton);
        returnButton = view.findViewById(R.id.returnToParentButton);

        // Load data
        loadProfile();

        // Edit PB button
        editPBButton.setOnClickListener(v -> showPBDialog());

        // Return to parent home
        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ParentActivity.class);
            intent.putExtra("uid", parentUid);
            intent.putExtra("role", "parent");
            startActivity(intent);
            requireActivity().finish();
        });
    }


    // ================================
    // Load Child Profile
    // ================================
    private void loadProfile() {
        db.collection("children")
                .document(childUid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(requireContext(), "Child not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    String username = doc.getString("username");
                    String dob = doc.getString("dob");
                    Double pb = doc.getDouble("pb");

                    // Display first + last name
                    String fullName = (first != null ? first : "")
                            + " "
                            + (last != null ? last : "");
                    childName.setText(fullName.trim());

                    // Username under name
                    childUsername.setText(username != null ? username : "-");

                    dobValue.setText(dob != null ? dob : "-");
                    pbValue.setText(pb != null ? String.valueOf(pb) : "0");

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load child data", Toast.LENGTH_SHORT).show();
                    Log.e("ParentProfile", "Error loading child profile", e);
                });
    }


    // ==========================
    // Edit PB Dialog
    // ==========================
    private void showPBDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Personal Best");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter new PB");

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {

            String value = input.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(requireContext(), "PB cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            double newPB;
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

    // ==========================
    // Save PB to Firestore
    // ==========================
    private void savePB(double newPB) {

        db.collection("children")
                .document(childUid)
                .update("pb", newPB)
                .addOnSuccessListener(aVoid -> {

                    pbValue.setText(String.valueOf(newPB));
                    Toast.makeText(requireContext(), "PB updated!", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to update PB", Toast.LENGTH_SHORT).show()
                );
    }
}
