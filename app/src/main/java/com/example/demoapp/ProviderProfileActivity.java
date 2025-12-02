package com.example.demoapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity responsible for displaying and editing a provider's profile information.
 *
 * <p>This screen allows a logged-in provider to view and update basic details
 * such as first name and last name. All data is loaded from and written to
 * the Firestore "users" collection.</p>
 *
 * <p>The activity also includes a back button, a logout button, and editing tools
 * via an AlertDialog for updating fields.</p>
 */
public class ProviderProfileActivity extends AppCompatActivity {

    /** UI text field displaying provider's full name. */
    private TextView providerName;

    /** UI text field displaying provider's username. */
    private TextView usernameValue;

    /** UI field showing the provider's first name. */
    private TextView firstNameValue;

    /** UI field showing the provider's last name. */
    private TextView lastNameValue;

    /** UI field showing provider's email. */
    private TextView emailValue;

    /** Button to edit first name. */
    private Button editFirstButton;

    /** Button to edit last name. */
    private Button editLastButton;

    /** Button that signs the provider out. */
    private Button logoutButton;

    /** Provider profile avatar display (placeholder image). */
    private ImageView profileImage;

    /** Back navigation button (finishes the activity). */
    private ImageView backButton;

    /** Firestore reference used for profile reads and updates. */
    private FirebaseFirestore db;

    /** UID of the logged-in provider (passed from previous screen). */
    private String providerUid;

    /**
     * Called when the activity is created.
     * Initializes UI components, loads provider data, and sets up listeners.
     *
     * @param savedInstanceState saved state bundle (unused)
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_profile);

        db = FirebaseFirestore.getInstance();

        // Retrieve provider UID passed through intent
        providerUid = getIntent().getStringExtra("uid");

        if (providerUid == null) {
            Toast.makeText(this, "Missing provider ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ---------------------------------------------------
        // Bind UI elements to XML IDs
        // ---------------------------------------------------
        backButton = findViewById(R.id.backButton);
        profileImage = findViewById(R.id.profileImage);
        providerName = findViewById(R.id.providerName);
        usernameValue = findViewById(R.id.usernameValue);
        firstNameValue = findViewById(R.id.firstNameValue);
        lastNameValue = findViewById(R.id.lastNameValue);
        emailValue = findViewById(R.id.emailValue);

        editFirstButton = findViewById(R.id.editFirstNameButton);
        editLastButton = findViewById(R.id.editLastNameButton);
        logoutButton = findViewById(R.id.providerLogoutButton);

        // Back button simply finishes the activity
        backButton.setOnClickListener(v -> finish());

        // Load Firestore profile info
        loadProviderProfile();

        // Setup edit dialog handlers
        editFirstButton.setOnClickListener(v -> showEditDialog("firstName"));
        editLastButton.setOnClickListener(v -> showEditDialog("lastName"));

        // Logout button: sign out and go back to login
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProviderProfileActivity.this, SignInActivity.class));
            finish();
        });
    }

    /**
     * Loads the provider's profile information from Firestore.
     *
     * <p>This retrieves fields from the "users" document corresponding to the
     * provider's UID. If successful, it updates the UI. If not, an error
     * message is displayed.</p>
     */
    private void loadProviderProfile() {
        db.collection("users")
                .document(providerUid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Read Firestore data
                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    String username = doc.getString("username");
                    String email = doc.getString("email");

                    // Set full name
                    providerName.setText(((first != null ? first : "") + " " +
                            (last != null ? last : "")).trim());

                    usernameValue.setText(username != null ? username : "-");
                    firstNameValue.setText(first != null ? first : "-");
                    lastNameValue.setText(last != null ? last : "-");
                    emailValue.setText(email != null ? email : "-");

                })
                .addOnFailureListener(e -> {
                    Log.e("ProviderProfile", "Error loading profile", e);
                });
    }

    /**
     * Opens an AlertDialog allowing the provider
     * to edit either the first or last name.
     *
     * @param fieldKey The Firestore key to edit ("firstName" or "lastName")
     */
    private void showEditDialog(String fieldKey) {

        String title =
                fieldKey.equals("firstName")
                        ? "Edit First Name"
                        : "Edit Last Name";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        // Input box for editing text
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // Pre-fill current value
        if (fieldKey.equals("firstName")) {
            input.setText(firstNameValue.getText().toString());
        } else {
            input.setText(lastNameValue.getText().toString());
        }

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = input.getText().toString().trim();

            if (value.isEmpty()) {
                Toast.makeText(this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            saveUpdatedField(fieldKey, value);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Saves the updated name field to Firestore,
     * then updates the UI to reflect the change.
     *
     * @param key   Firestore field key ("firstName" or "lastName")
     * @param value New value to store
     */
    private void saveUpdatedField(String key, String value) {

        db.collection("users")
                .document(providerUid)
                .update(key, value)
                .addOnSuccessListener(aVoid -> {

                    // Update UI immediately for better user feedback
                    if (key.equals("firstName")) {
                        firstNameValue.setText(value);
                    } else {
                        lastNameValue.setText(value);
                    }

                    // Update full display name
                    providerName.setText(
                            firstNameValue.getText().toString() + " " +
                                    lastNameValue.getText().toString()
                    );

                    Toast.makeText(this, "Updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
