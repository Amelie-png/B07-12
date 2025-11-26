package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demoapp.login.LoginContract;
import com.example.demoapp.login.LoginPresenter;

public class SignInActivity extends AppCompatActivity implements LoginContract.View {

    private EditText emailOrUsernameInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView signupText;
    private TextView forgotPasswordText;
    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_screen);

        presenter = new LoginPresenter(this);

        // UI References
        emailOrUsernameInput = findViewById(R.id.email_username);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.log_in_button);
        signupText = findViewById(R.id.back_to_sign_up);
        forgotPasswordText = findViewById(R.id.forget_password);

        // LOGIN BUTTON LOGIC
        loginButton.setOnClickListener(v -> {
            String identifier = emailOrUsernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            presenter.handleLogin(identifier, password);
        });

        // SIGNUP NAVIGATION → CreateAccountActivity
        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        // FORGOT PASSWORD NAVIGATION → PasswordRecoveryActivity
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, PasswordRecoveryActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void showLoading() {}

    @Override
    public void hideLoading() {}

    @Override
    public void showError(String message) {
        runOnUiThread(() ->
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void showSuccess(String message) {
        runOnUiThread(() ->
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
        );
    }

    // ======================================================
    // UPDATED NAVIGATION METHODS WITH CORRECT ACTIVITIES
    // ======================================================

    @Override
    public void navigateToChildHome(String childId) {
        Intent intent = new Intent(this, MainNavActivity.class);
        intent.putExtra("uid", childId);
        intent.putExtra("role", "child");
        startActivity(intent);
        finish();
    }

    @Override
    public void navigateToParentIdentitySelection(String parentUid) {
        Intent intent = new Intent(this, ChooseIdentityActivity.class);
        intent.putExtra("parentUid", parentUid);
        startActivity(intent);
        finish();
    }

    @Override
    public void navigateToProviderHome(String providerUid) {
        // We will later replace this with ProviderNavActivity
        Intent intent = new Intent(this, MainNavActivity.class);
        intent.putExtra("uid", providerUid);
        intent.putExtra("role", "provider");
        startActivity(intent);
        finish();
    }
}
