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

/**
 * Fragment that provides a simplified password recovery interface.
 *
 * <p>This version of password recovery does not actually integrate with
 * Firebase Authentication. Instead, it simulates the behavior by showing
 * a confirmation Toast when the user enters an email.</p>
 *
 * <p>This fragment may be used as part of a mixed navigation flow where
 * some activities use fragments instead of full screens.</p>
 */
public class PasswordRecoveryFragment extends Fragment {

    /**
     * Default constructor that inflates the password recovery layout.
     * <p>This uses the Fragment(R.layout.xxx) constructor pattern.</p>
     */
    public PasswordRecoveryFragment() {
        super(R.layout.activity_password_recovery);
    }

    /**
     * Called after the fragment's view has been created.
     * <p>This method binds UI components and defines the behavior of the
     * "Send Recovery Email" button. Unlike the Activity version, this does
     * not send an actual Firebase reset email.</p>
     *
     * @param view The root inflated view
     * @param savedInstanceState Saved state (unused)
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // -----------------------------------------
        // Bind XML components
        // -----------------------------------------
        EditText emailInput = view.findViewById(R.id.recoveryEmailInput);
        Button sendButton = view.findViewById(R.id.sendRecoveryButton);

        // -----------------------------------------
        // Button click -> validate + show Toast
        // -----------------------------------------
        sendButton.setOnClickListener(v -> {

            String email = emailInput.getText().toString().trim();

            // Missing email
            if (email.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Please enter your email address.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Simulated success message
            Toast.makeText(
                    requireContext(),
                    "Recovery email sent successfully to " + email,
                    Toast.LENGTH_SHORT
            ).show();

            // Clear input field
            emailInput.setText("");
        });
    }
}
