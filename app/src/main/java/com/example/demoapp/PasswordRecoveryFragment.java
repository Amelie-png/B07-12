package com.example.demoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class PasswordRecoveryFragment extends Fragment {
    public PasswordRecoveryFragment() {
        super(R.layout.activity_password_recovery);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Connect to your XML components
        EditText emailInput = view.findViewById(R.id.recoveryEmailInput);
        Button sendButton = view.findViewById(R.id.sendRecoveryButton);

        // Set what happens when the button is clicked
        sendButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                // Show error Toast
                Toast.makeText(
                        requireContext(),
                        "Please enter your email address.",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                // Show success Toast
                Toast.makeText(
                        requireContext(),
                        "Recovery email sent successfully to " + email,
                        Toast.LENGTH_SHORT
                ).show();

                // Optional: clear input after success
                emailInput.setText("");
            }
        });
    }
}
