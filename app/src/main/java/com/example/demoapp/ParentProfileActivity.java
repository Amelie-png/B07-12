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

public class ParentProfileActivity extends AppCompatActivity {

    private TextView providerName;
    private TextView usernameValue;
    private TextView firstNameValue;
    private TextView lastNameValue;
    private TextView emailValue;

    private Button editFirstButton;
    private Button editLastButton;
    private Button logoutButton;

    private ImageView profileImage;
    private ImageView backButton;    // <-- NEW

    private FirebaseFirestore db;
    private String parentUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_profile);

        db = FirebaseFirestore.getInstance();
        parentUid = getIntent().getStringExtra("uid");

        if (parentUid == null) {
            Toast.makeText(this, "Missing parent ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // -----------------------------
        // Bind UI Elements
        // -----------------------------
        backButton = findViewById(R.id.backButton);   // <-- NEW
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

        // Load user profile
        loadParentProfile();

        // Edit Buttons
        editFirstButton.setOnClickListener(v -> showEditDialog("firstName"));
        editLastButton.setOnClickListener(v -> showEditDialog("lastName"));

        // Logout Button
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ParentProfileActivity.this, SignInActivity.class));
            finish();
        });
    }

    // -----------------------------
    // Load Parent Info from Firestore
    // -----------------------------
    private void loadParentProfile() {
        db.collection("users")
                .document(parentUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String first = doc.getString("firstName");
                    String last = doc.getString("lastName");
                    String username = doc.getString("username");
                    String email = doc.getString("email");

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

    // -----------------------------
    // Edit Dialog
    // -----------------------------
    private void showEditDialog(String fieldKey) {
        String title = fieldKey.equals("firstName") ? "Edit First Name" : "Edit Last Name";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        if (fieldKey.equals("firstName")) {
            input.setText(firstNameValue.getText().toString());
        } else {
            input.setText(lastNameValue.getText().toString());
        }

        builder.setView(input);

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

    // -----------------------------
    // Save Updated Profile Field
    // -----------------------------
    private void saveUpdatedField(String key, String value) {
        db.collection("users")
                .document(parentUid)
                .update(key, value)
                .addOnSuccessListener(aVoid -> {

                    if (key.equals("firstName")) {
                        firstNameValue.setText(value);
                    } else {
                        lastNameValue.setText(value);
                    }

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
