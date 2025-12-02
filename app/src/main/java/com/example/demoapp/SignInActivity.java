package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demoapp.login.LoginContract;
import com.example.demoapp.login.LoginPresenter;

/**
 * Activity responsible for handling user login.
 *
 * <p>This screen allows the user to sign in using either an email
 * address or a username. It follows the MVP architecture pattern,
 * where this activity is the View and delegates authentication logic
 * to {@link LoginPresenter}.</p>
 *
 * <p>Depending on the login result and the account type, the user is
 * navigated to Child, Parent, or Provider home screens.</p>
 */
public class SignInActivity extends AppCompatActivity implements LoginContract.View {

    /** Input field for the user's email OR username. */
    private EditText emailOrUsernameInput;

    /** Input field for the user's account password. */
    private EditText passwordInput;

    /** Button used to trigger login. */
    private Button loginButton;

    /** Text link that navigates to account creation screen. */
    private TextView signupText;

    /** Text link that navigates to password recovery screen. */
    private TextView forgotPasswordText;

    /** Presenter that handles all login business logic (MVP pattern). */
    private LoginPresenter presenter;

    /**
     * Called when the activity is created.
     *
     * <p>Initializes UI elements, sets up button click listeners,
     * and prepares the login presenter to respond to user input.</p>
     *
     * @param savedInstanceState previously saved state bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_screen);

        // Presenter for login logic
        presenter = new LoginPresenter(this);

        // -----------------------------
        // Bind UI components
        // -----------------------------
        emailOrUsernameInput = findViewById(R.id.email_username);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.log_in_button);
        signupText = findViewById(R.id.back_to_sign_up);
        forgotPasswordText = findViewById(R.id.forget_password);

        // -----------------------------------------
        // LOGIN BUTTON → Run presenter login logic
        // -----------------------------------------
        loginButton.setOnClickListener(v -> {
            String identifier = emailOrUsernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            presenter.handleLogin(identifier, password);
        });

        // -----------------------------------------
        // SIGNUP NAVIGATION → CreateAccountActivity
        // -----------------------------------------
        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        // ----------------------------------------------------
        // FORGOT PASSWORD NAVIGATION → PasswordRecoveryActivity
        // ----------------------------------------------------
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, PasswordRecoveryActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Shows a loading indicator during login.
     *
     * <p>(Not yet implemented visually — kept for MVP structure compliance.)</p>
     */
    @Override
    public void showLoading() {}

    /**
     * Hides the loading indicator after login completes.
     *
     * <p>(Not yet implemented visually — reserved for future progress bar.)</p>
     */
    @Override
    public void hideLoading() {}

    /**
     * Displays an error message on the login screen.
     *
     * @param message text to show in a Toast notification
     */
    @Override
    public void showError(String message) {
        runOnUiThread(() ->
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Displays a success message (rarely used; login usually navigates).
     *
     * @param message text to show in a Toast notification
     */
    @Override
    public void showSuccess(String message) {
        runOnUiThread(() ->
                android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
        );
    }

    // ======================================================
    // NAVIGATION METHODS — Called by LoginPresenter
    // ======================================================

    /**
     * Navigates to the child's home navigation graph.
     *
     * @param childId the UID of the logged-in child
     */
    @Override
    public void navigateToChildHome(String childId) {
        Intent intent = new Intent(this, MainNavActivity.class);
        intent.putExtra("uid", childId);
        intent.putExtra("role", "child");
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to the parent identity selection screen.
     *
     * <p>Parents may choose themselves or one of their children to log in as.</p>
     *
     * @param parentUid the UID of the parent
     */
    @Override
    public void navigateToParentIdentitySelection(String parentUid) {
        Intent intent = new Intent(this, ChooseIdentityActivity.class);
        intent.putExtra("parentUid", parentUid);
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to the provider's main activity (dashboard).
     *
     * @param providerUid the UID of the provider
     */
    @Override
    public void navigateToProviderHome(String providerUid) {
        // Temporary mapping to ProviderMain (replace later)
        Intent intent = new Intent(this, ProviderMain.class);
        intent.putExtra("uid", providerUid);
        intent.putExtra("role", "provider");
        startActivity(intent);
        finish();
    }
}
