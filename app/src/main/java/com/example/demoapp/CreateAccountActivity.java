package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demoapp.signup.SignupContract;
import com.example.demoapp.signup.SignupPresenter;


public class CreateAccountActivity extends AppCompatActivity implements SignupContract.View {

    private EditText firstNameField, lastNameField, usernameField, emailField, passwordField, confirmPasswordField;
    private Spinner roleSpinner;
    private Button signupButton;
    private TextView signInLink;

    private SignupPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        presenter = new SignupPresenter(this);

        // ------------------------
        // 1. CONNECT UI COMPONENTS
        // ------------------------
        roleSpinner = findViewById(R.id.spinnerRoles);
        firstNameField = findViewById(R.id.signUpFirst);
        lastNameField = findViewById(R.id.signUpLast);
        usernameField = findViewById(R.id.signUpUsername);
        emailField = findViewById(R.id.signUpEmail);
        passwordField = findViewById(R.id.signUpPassword);
        confirmPasswordField = findViewById(R.id.signUpConfirmPassword);
        signupButton = findViewById(R.id.signUpButton);
        signInLink = findViewById(R.id.sign_in_link);

        // ------------------------
        // 2. ROLE SPINNER SETUP
        // ------------------------
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.role_items,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        roleSpinner.setSelection(0, false);
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0){
                    String selectedRole = parent.getItemAtPosition(position).toString();
                    Toast.makeText(CreateAccountActivity.this, selectedRole, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // ------------------------
        // 3. SIGNUP BUTTON
        // ------------------------
        signupButton.setOnClickListener(v -> {
            String role = roleSpinner.getSelectedItem().toString();

            presenter.handleSignup(
                    firstNameField.getText().toString().trim(),
                    lastNameField.getText().toString().trim(),
                    usernameField.getText().toString().trim(),
                    emailField.getText().toString().trim(),
                    passwordField.getText().toString().trim(),
                    confirmPasswordField.getText().toString().trim(),
                    role
            );
        });

        // ------------------------
        // 4. NAVIGATE TO LOGIN
        // ------------------------
        signInLink.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });
    }


    // ============================
    // MVP View Interface Methods
    // ============================

    @Override
    public void showLoading() {
        Toast.makeText(this, "Creating account…", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void hideLoading() {
        // Add progress bar later if you want
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}
