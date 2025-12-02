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
 * Activity responsible for displaying and allowing edits to a
 * parent user's profile information (name, username, email).
 *
 * <p>This screen allows a parent to:
 * <ul>
 *     <li>View their full profile details</li>
 *     <li>Edit their first and last name</li>
 *     <li>Navigate back to the previous screen</li>
 *     <li>Log out of the application</li>
 * </ul>
 *
 * Profile data is retrieved from Firestore under the "users" collection.
 */
public class ParentProfileActivity extends AppCompatActivity {

    /** Text view showing the parent’s full name (first + last). */
    private TextView providerName;

    /** Text view showing the parent’s username. */
    private TextView usernameValue;

    /** Text view displaying the parent's first name. */
    private TextView firstNameValue;

    /** Text view displaying the parent's last name. */
    private TextView lastNameValue;

    /** Text view showing the parent’s email. */
    private TextView emailValue;

    /** Button for editing the first name field. */
    private Button editFirstButton;

    /** Button for editing the last name field. */
    private Button editLastButton;

    /** Logout button. */
    private Button logoutButton;

    /** Profile image placeholder. */
    private ImageView profileImage;

    /** Back navigation button. */
    private ImageView backButton;

    /** Firestore instance used to retrieve and update user data. */
    private FirebaseFirestore db;

    /** UID of the parent whose profile is being displayed. */
    private String parentUid;

    /**
     * Lifecycle callback that initializes the Activity UI and loads profile data.
     *
     * @param savedInstanceState Saved state (unused).
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_profile);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve UID from previous Activity
        parentUid = getIntent().getStringExtra("uid");

        if (parentUid == null) {
            Toast.makeText(this, "Missing parent ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // -----------------------------
        // Bind UI Elements
        // -----------------------------
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

        // -----------------------------
        // Back Button Behavior
        // -----------------------------
        backButton.setOnClickListener(v -> finish());

        // -----------------------------
        // Load Profile Data
        // -----------------------------
        loadParentProfile();

        // -----------------------------
        // Edit First/Last Name Handlers
        // -----------------------------
        editFirstButton.setOnClickListener(v -> showEditDialog("firstName"));
        editLastButton.setOnClickListener(v -> showEditDialog("lastName"));

        // -----------------------------
        // Logout Logic
        // -----------------------------
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ParentProfileActivity.this, SignInActivity.class));
            finish();
        });
    }

    /**
     * Retrieves the parent profile document from Firestore and updates UI fields.
     * Displays an error if the profile does not exist or cannot be retrieved.
     */
    private void loadParentProfile() {
        db.collection("users")
                .document(parentUid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Extract fields
                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    String username = doc.getString("username");
                    String email = doc.getString("email");

                    // Full name display
                    providerName.setText(((first != null ? first : "") + " " +
                            (last != null ? last : "")).trim());

                    usernameValue.setText(username != null ? username : "-");
                    firstNameValue.setText(first != null ? first : "-");
                    lastNameValue.setText(last != null ? last : "-");
                    emailValue.setText(email != null ? email : "-");

                })
                .addOnFailureListener(e ->
                        Log.e("ParentProfile", "Error loading profile", e)
                );
    }

    /**
     * Displays a dialog allowing the parent to edit either their first
     * or last name. Saves the update to Firestore if confirmed.
     *
     * @param fieldKey The Firestore field name to update ("firstName" or "lastName").
     */
    private void showEditDialog(String fieldKey) {

        String title = fieldKey.equals("firstName")
                ? "Edit First Name"
                : "Edit Last Name";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        // Input box for updated name
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // Prefill current value
        if (fieldKey.equals("firstName")) {
            input.setText(firstNameValue.getText().toString());
        } else {
            input.setText(lastNameValue.getText().toString());
        }

        builder.setView(input);

        // Confirm save
        builder.setPositiveButton("Save", (dialog, which) -> {
            String value = input.getText().toString().trim();

            if (value.isEmpty()) {
                Toast.makeText(this, "Value cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            saveUpdatedField(fieldKey, value);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Saves a changed profile field (first name or last name) to Firestore
     * and updates the UI accordingly.
     *
     * @param key   The Firestore field name to update.
     * @param value The new value to save.
     */
    private void saveUpdatedField(String key, String value) {

        db.collection("users")
                .document(parentUid)
                .update(key, value)
                .addOnSuccessListener(aVoid -> {

                    // Update text fields
                    if (key.equals("firstName")) {
                        firstNameValue.setText(value);
                    } else {
                        lastNameValue.setText(value);
                    }

                    // Rebuild full name
                    providerName.setText(
                            firstNameValue.getText().toString() + " " +
                                    lastNameValue.getText().toString()
                    );

                    Toast.makeText(this, "Updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Update failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}
