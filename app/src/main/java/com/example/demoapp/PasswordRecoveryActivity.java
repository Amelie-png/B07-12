package com.example.demoapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PasswordRecoveryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password_recovery);

        // Keep your existing insets code
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect to your XML components
        EditText emailInput = findViewById(R.id.recoveryEmailInput);
        Button sendButton = findViewById(R.id.sendRecoveryButton);

        // Set what happens when the button is clicked
        sendButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                // Show error Toast
                Toast.makeText(
                        PasswordRecoveryActivity.this,
                        "Please enter your email address.",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                // Show success Toast
                Toast.makeText(
                        PasswordRecoveryActivity.this,
                        "Recovery email sent successfully to " + email,
                        Toast.LENGTH_SHORT
                ).show();

                // Optional: clear input after success
                emailInput.setText("");
            }
        });
    }
}