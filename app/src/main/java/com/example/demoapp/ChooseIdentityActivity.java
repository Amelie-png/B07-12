package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChooseIdentityActivity extends AppCompatActivity {

    private String parentUid;
    private LinearLayout containerButtons;
    private Button parentHomeButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_identity);

        // ============================
        // 1. GET PARENT ID (logged-in user)
        // ============================
        parentUid = getIntent().getStringExtra("parentUid");

        if (parentUid == null) {
            Toast.makeText(this, "Error: missing parent ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ============================
        // 2. UI SETUP
        // ============================
        containerButtons = findViewById(R.id.container_identity_buttons);
        parentHomeButton = findViewById(R.id.button_parent_home);

        // ----------------------------
        // Parent → MainNavActivity
        // ----------------------------
        parentHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseIdentityActivity.this, MainNavActivity.class);
            intent.putExtra("uid", parentUid);
            intent.putExtra("role", "parent");
            startActivity(intent);
            finish();
        });

        // ============================
        // 3. Load children dynamically
        // ============================
        listenToChildAccountsRealtime();
    }

    // =====================================================
    // REAL-TIME LISTENER FOR CHILD ACCOUNTS
    // =====================================================
    private void listenToChildAccountsRealtime() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("children")
                .whereEqualTo("parentUid", parentUid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading children", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Keep only 1st button (parent)
                    int count = containerButtons.getChildCount();
                    if (count > 1) {
                        containerButtons.removeViews(1, count - 1);
                    }

                    if (value == null || value.isEmpty()) {
                        return;
                    }

                    for (DocumentSnapshot childDoc : value) {
                        String childName = childDoc.getString("username");
                        String childId = childDoc.getId();

                        Button childButton = new Button(this);
                        childButton.setText("Log in as " + childName);

                        // ----------------------------
                        // Child → MainNavActivity
                        // ----------------------------
                        childButton.setOnClickListener(v -> {
                            Intent intent = new Intent(ChooseIdentityActivity.this, MainNavActivity.class);
                            intent.putExtra("uid", childId);
                            intent.putExtra("role", "child");
                            startActivity(intent);
                            finish();
                        });

                        containerButtons.addView(childButton);
                    }
                });
    }
}
