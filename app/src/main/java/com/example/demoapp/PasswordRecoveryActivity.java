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

public class PasswordRecoveryActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password_recovery);

        // Keep your existing insets logic
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        // Connect UI components
        EditText emailInput = findViewById(R.id.recoveryEmailInput);
        Button sendButton = findViewById(R.id.sendRecoveryButton);

        // On click -> send recovery email
        sendButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            // Validate empty input
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email format
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send password reset email
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Show success message
                            Toast.makeText(
                                    this,
                                    "A password reset link was sent to " + email,
                                    Toast.LENGTH_LONG
                            ).show();

                            // Optional: clear input
                            emailInput.setText("");

                            // Navigate back to Sign-In screen
                            Intent intent = new Intent(this, SignInActivity.class);
                            startActivity(intent);
                            finish(); // prevent returning to this screen
                        } else {
                            // Error handling
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
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                    });
        });
    }
}
