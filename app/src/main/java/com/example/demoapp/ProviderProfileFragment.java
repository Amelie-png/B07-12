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

public class ProviderProfileFragment extends Fragment {

    private TextView childNameView;
    private TextView childUsernameView;
    private TextView dobValueView;
    private ImageView childImageView;
    private Button returnButton;

    private String childUid;
    private String providerUid;

    private FirebaseFirestore db;

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

        // ------------------------------------------
        //  ⭐ READ ARGUMENTS EXACT SAME AS PARENT
        // ------------------------------------------
        if (getArguments() != null) {
            childUid = getArguments().getString("uid");          // <-- CHILD ID (same as parent)
            providerUid = getArguments().getString("providerUid");
        }

        if (childUid == null) {
            Toast.makeText(requireContext(), "Missing child ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bind UI
        childNameView = view.findViewById(R.id.providerChildName);
        childUsernameView = view.findViewById(R.id.providerChildUsername);
        dobValueView = view.findViewById(R.id.providerDobValue);
        childImageView = view.findViewById(R.id.profileImageProviderChild);
        returnButton = view.findViewById(R.id.returnToProviderButton);

        loadChildProfile();

        // Return to Provider Main
        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ProviderMain.class);
            intent.putExtra("uid", providerUid);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    // ----------------------------------------------------
    // Load child's profile
    // ----------------------------------------------------
    private void loadChildProfile() {

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

                    String fullName = ((first != null) ? first : "") + " " + ((last != null) ? last : "");
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
