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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChildProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView childName;
    private TextView dobValue;
    private Button logoutButton;

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

        // ---- Retrieve UID passed from MainNavActivity ----
        if (getArguments() != null) {
            childUid = getArguments().getString("uid");
        }
        Log.d("ChildProfileFragment", "childUid = " + childUid);

        // Bind UI elements
        profileImage = view.findViewById(R.id.profileImage);
        childName = view.findViewById(R.id.childName);
        dobValue = view.findViewById(R.id.dobValue);
        logoutButton = view.findViewById(R.id.logoutButton);

        loadChildProfile();

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadChildProfile() {
        if (childUid == null) {
            Toast.makeText(requireContext(), "Missing child ID", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("children")
                .document(childUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(requireContext(), "Child profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String username = doc.getString("username");
                    String dob = doc.getString("dob");

                    // Update UI
                    childName.setText(username != null ? username : "Unknown Child");
                    dobValue.setText(dob != null ? dob : "-");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load child profile", Toast.LENGTH_SHORT).show();
                    Log.e("ChildProfile", "Error loading profile", e);
                });
    }
}
