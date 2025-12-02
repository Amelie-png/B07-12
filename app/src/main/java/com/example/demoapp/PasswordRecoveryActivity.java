package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

/**
 * Activity that allows users to reset their password by entering their email.
 *
 * <p>This screen:
 * <ul>
 *     <li>Accepts an email address from the user</li>
 *     <li>Validates the email format</li>
 *     <li>Sends a Firebase password-reset link</li>
 *     <li>Navigates the user back to the sign-in screen</li>
 * </ul>
 *
 * This activity is used for both parents, providers, and children,
 * since Firebase Authentication handles all account types.
 */
public class PasswordRecoveryActivity extends AppCompatActivity {

    /** FirebaseAuth instance used to send password reset emails. */
    private FirebaseAuth auth;

    /**
     * Initializes the Activity, sets up UI elements, and attaches listeners.
     *
     * @param savedInstanceState Previously saved Activity state (unused)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password_recovery);

        // -----------------------------
        // Apply window insets for full-screen layout
        // -----------------------------
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase Authentication reference
        auth = FirebaseAuth.getInstance();

        // -----------------------------
        // Bind UI components
        // -----------------------------
        EditText emailInput = findViewById(R.id.recoveryEmailInput);
        Button sendButton = findViewById(R.id.sendRecoveryButton);

        // -----------------------------
        // Send password reset email
        // -----------------------------
        sendButton.setOnClickListener(v -> {

            String email = emailInput.getText().toString().trim();

            // Empty email validation
            if (email.isEmpty()) {
                Toast.makeText(
                        this,
                        "Please enter your email address.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Email format validation
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(
                        this,
                        "Please enter a valid email address.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // -----------------------------
            // Send Firebase reset email
            // -----------------------------
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            // Success message
                            Toast.makeText(
                                    this,
                                    "A password reset link was sent to " + email,
                                    Toast.LENGTH_LONG
                            ).show();

                            // Clear text field after success
                            emailInput.setText("");

                            // Navigate user back to sign-in screen
                            Intent intent = new Intent(this, SignInActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }

                        // -----------------------------
                        // Error handling
                        // -----------------------------
                        Exception e = task.getException();

                        if (e instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(
                                    this,
                                    "This email is not registered.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Error: " + (e != null ? e.getMessage() : "Unknown error"),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                    });
        });
    }
}
