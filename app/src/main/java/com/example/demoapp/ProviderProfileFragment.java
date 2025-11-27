package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class ProviderProfileFragment extends Fragment {
    private String providerUid;
    private ImageView profileImage;
    private TextView name;
    private TextView username;
    private TextView email;
    private TextView forgetPassword;
    private Button logOutButton;

    public ProviderProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_provider_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            providerUid = getArguments().getString("uid");
        }

        Log.d("ProviderProfileFragment", "providerUid = " + providerUid);

        // Bind UI elements
        profileImage = view.findViewById(R.id.profileImage);
        name = view.findViewById(R.id.providerName);
        username = view.findViewById(R.id.usernameValue);
        email = view.findViewById(R.id.emailValue);
        forgetPassword = view.findViewById(R.id.provider_profile_forget_password);
        logOutButton = view.findViewById(R.id.logoutButton);

        // Temporary hardcoded data — replace with Firebase later
        name.setText("Provider Name");
        username.setText("username123");
        email.setText("123@mail.com");

        setUpForgetPassword();
        setUpLogOut();
    }

    private void setUpForgetPassword(){
        forgetPassword.setOnClickListener(v -> {
            Fragment next = new ProviderPasswordRecovery();
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    //TODO: replace R.id with the actual fragment container name
                    .replace(R.id.fragment_container, next)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setUpLogOut(){
        //copied from ChildProfileFragment
        //TODO: create a Utils file for all profile fragments.
        logOutButton.setOnClickListener(v -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // Go back to LoginActivity
            Intent intent = new Intent(requireActivity(), SignInActivity.class);

            // Clear the back stack so user cannot press back to return
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        });
    }
}