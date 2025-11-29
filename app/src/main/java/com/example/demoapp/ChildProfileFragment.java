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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChildProfileFragment extends Fragment {

    private TextView childName;
    private TextView childUsername;
    private TextView dobValue;
    private Button logoutButton;

    private FirebaseFirestore db;
    private String childUid;

    public ChildProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Retrieve child UID from MainNavActivity argument bundle
        if (getArguments() != null) {
            childUid = getArguments().getString("uid");
        }

        if (childUid == null) {
            Toast.makeText(requireContext(), "Missing child ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bind UI
        childName = view.findViewById(R.id.childName);
        childUsername = view.findViewById(R.id.childUsername);
        dobValue = view.findViewById(R.id.dobValue);
        logoutButton = view.findViewById(R.id.logoutButton);

        loadProfile();

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), SignInActivity.class);
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
                        Toast.makeText(requireContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    String username = doc.getString("username");
                    String dob = doc.getString("dob");

                    // Set full name
                    String fullName = ((first != null) ? first : "") + " " + ((last != null) ? last : "");
                    childName.setText(fullName.trim().isEmpty() ? "Unknown" : fullName);

                    // Username
                    childUsername.setText(username != null ? username : "-");

                    // DOB
                    dobValue.setText(dob != null ? dob : "-");

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                    Log.e("ChildProfile", "Error loading child profile", e);
                });
    }
}
