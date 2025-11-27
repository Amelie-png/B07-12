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

import com.google.firebase.auth.FirebaseAuth;

public class ChildProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView childName;
    private TextView usernameValue;
    private TextView dobValue;
    private Button logoutButton;

    private String childUid;

    public ChildProfileFragment() {
        // Required empty public constructor
    }

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

        // ---- Retrieve UID argument ----
        if (getArguments() != null) {
            childUid = getArguments().getString("uid");
        }
        Log.d("ChildProfileFragment", "childUid = " + childUid);

        // Bind UI elements
        profileImage = view.findViewById(R.id.profileImage);
        childName = view.findViewById(R.id.childName);
        usernameValue = view.findViewById(R.id.usernameValue);
        dobValue = view.findViewById(R.id.dobValue);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Temporary hardcoded data — replace with Firebase later
        childName.setText("Child Name");
        usernameValue.setText("username123");
        dobValue.setText("2010-05-15");

        // Handle logout click
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(requireActivity(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
