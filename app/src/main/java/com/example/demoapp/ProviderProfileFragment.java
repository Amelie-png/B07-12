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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProviderProfileFragment extends Fragment {

    private TextView providerName;     // Full name under the title
    private TextView usernameValue;    // Username displayed beside image
    private TextView firstNameValue;   // Editable
    private TextView lastNameValue;    // Editable
    private TextView emailValue;       // Email display

    private Button editFirstNameButton;
    private Button editLastNameButton;
    private Button logoutButton;

    private FirebaseFirestore db;
    private String providerUid;

    public ProviderProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provider_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            providerUid = getArguments().getString("uid");
        }

        if (providerUid == null) {
            Toast.makeText(requireContext(), "Missing provider ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bind UI (MATCHING XML IDs EXACTLY)
        providerName = view.findViewById(R.id.providerName);
        usernameValue = view.findViewById(R.id.usernameValue);
        firstNameValue = view.findViewById(R.id.firstNameValue);
        lastNameValue = view.findViewById(R.id.lastNameValue);
        emailValue = view.findViewById(R.id.emailValue);

        editFirstNameButton = view.findViewById(R.id.editFirstNameButton);
        editLastNameButton = view.findViewById(R.id.editLastNameButton);
        logoutButton = view.findViewById(R.id.providerLogoutButton);

        // Load provider data
        loadProfile();

        // Button listeners
        editFirstNameButton.setOnClickListener(v -> showEditDialog("firstName"));
        editLastNameButton.setOnClickListener(v -> showEditDialog("lastName"));

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireActivity(), SignInActivity.class));
            requireActivity().finish();
        });
    }

    // ====================================
    // Load provider data from Firestore
    // ====================================
    private void loadProfile() {
        db.collection("users")
                .document(providerUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(requireContext(),
                                "Provider profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    String username = doc.getString("username");
                    String email = doc.getString("email");

                    // Update UI
                    providerName.setText(
                            (first != null ? first : "") + " " +
                                    (last != null ? last : "")
                    );

                    usernameValue.setText(username != null ? username : "-");
                    firstNameValue.setText(first != null ? first : "-");
                    lastNameValue.setText(last != null ? last : "-");
                    emailValue.setText(email != null ? email : "-");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to load provider profile", Toast.LENGTH_SHORT).show();
                    Log.e("ProviderProfile", "Error loading", e);
                });
    }

    // ====================================
    // Edit dialog for first / last name
    // ====================================
    private void showEditDialog(String field) {
        String title = field.equals("firstName") ?
                "Edit First Name" : "Edit Last Name";

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = input.getText().toString().trim();

            if (value.isEmpty()) {
                Toast.makeText(requireContext(), "Value cannot be empty",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            saveField(field, value);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // ====================================
    // Save updated field to Firestore
    // ====================================
    private void saveField(String key, String value) {
        db.collection("users")
                .document(providerUid)
                .update(key, value)
                .addOnSuccessListener(aVoid -> {

                    // Update text fields
                    if (key.equals("firstName")) {
                        firstNameValue.setText(value);
                    } else {
                        lastNameValue.setText(value);
                    }

                    // Update combined name
                    providerName.setText(
                            firstNameValue.getText().toString() + " " +
                                    lastNameValue.getText().toString()
                    );

                    Toast.makeText(requireContext(),
                            "Updated successfully!", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to update profile", Toast.LENGTH_SHORT).show()
                );
    }

}
