package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demoapp.signup.SignupContract;
import com.example.demoapp.signup.SignupPresenter;

/**
 * Activity that handles account creation (sign-up) for new users.
 *
 * <p>This class is the View component in the MVP architecture and delegates
 * all business logic to the {@link SignupPresenter}.</p>
 *
 * <p>Users can enter:</p>
 * <ul>
 *     <li>First Name</li>
 *     <li>Last Name</li>
 *     <li>Username</li>
 *     <li>Email</li>
 *     <li>Password + Confirm Password</li>
 *     <li>Role (Parent or Provider)</li>
 * </ul>
 *
 * <p>If the signup succeeds, the user is redirected to the login screen.</p>
 */
public class CreateAccountActivity extends AppCompatActivity implements SignupContract.View {

    /** First name field input. */
    private EditText firstNameField;

    /** Last name field input. */
    private EditText lastNameField;

    /** Username field input. */
    private EditText usernameField;

    /** Email field input. */
    private EditText emailField;

    /** Password field input. */
    private EditText passwordField;

    /** Confirm password field input. */
    private EditText confirmPasswordField;

    /** Spinner allowing the user to choose a role (Parent/Provider). */
    private Spinner roleSpinner;

    /** Button that triggers the signup process. */
    private Button signupButton;

    /** Link to navigate back to the login / sign-in screen. */
    private TextView signInLink;

    /** Presenter handling all signup logic. */
    private SignupPresenter presenter;

    /**
     * Initializes the activity, sets up UI components, configures the role spinner,
     * and attaches button listeners for signup and navigation.
     *
     * @param savedInstanceState Restored state (not used here).
     */
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
                R.array.role_items,                       // Parent/Provider list
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // Do not auto-trigger selection toast on first load
        roleSpinner.setSelection(0, false);

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                // Display role selection feedback (except first disabled item)
                if (position > 0) {
                    String selectedRole = parent.getItemAtPosition(position).toString();
                    Toast.makeText(CreateAccountActivity.this,
                            selectedRole, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // ------------------------
        // 3. SIGNUP BUTTON ACTION
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
        // 4. NAVIGATE TO LOGIN SCREEN
        // ------------------------
        signInLink.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });
    }


    // ============================
    // MVP View Interface Methods
    // ============================

    /**
     * Shows a temporary loading state (currently via Toast, but can later
     * be upgraded to a ProgressBar or custom loading dialog).
     */
    @Override
    public void showLoading() {
        Toast.makeText(this, "Creating account…", Toast.LENGTH_SHORT).show();
    }

    /**
     * Hides the loading state.
     * Currently unused because no progress bar is implemented yet.
     */
    @Override
    public void hideLoading() {
        // No loading indicator implemented yet
    }

    /**
     * Displays an error message to the user.
     *
     * @param message Error text describing the problem.
     */
    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Displays a success message and navigates the user back to the
     * sign-in screen so they can log in with their newly created account.
     *
     * @param message Success text to show the user.
     */
    @Override
    public void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}
