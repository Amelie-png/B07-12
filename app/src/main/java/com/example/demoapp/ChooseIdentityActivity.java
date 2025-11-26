package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ChooseIdentityActivity extends AppCompatActivity {

    private String parentUid;
    private FirebaseFirestore firestore;

    private LinearLayout containerButtons;
    private Button parentHomeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_identity);

        firestore = FirebaseFirestore.getInstance();

        // Get parent UID from login
        parentUid = getIntent().getStringExtra("parentUid");

        containerButtons = findViewById(R.id.container_identity_buttons);
        parentHomeButton = findViewById(R.id.button_parent_home);

        // Parent Home button click
        parentHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseIdentityActivity.this, ParentHomeScreen.class);
            intent.putExtra("parentUid", parentUid);
            startActivity(intent);
        });

        // Load child accounts in real-time
        listenToChildAccountsRealtime();
    }

    private void listenToChildAccountsRealtime() {

        firestore.collection("children")
                .whereEqualTo("parentUid", parentUid)
                .addSnapshotListener((querySnapshot, error) -> {

                    if (error != null || querySnapshot == null) {
                        return;
                    }

                    for (DocumentChange change : querySnapshot.getDocumentChanges()) {

                        QueryDocumentSnapshot doc = change.getDocument();
                        String childId = doc.getId();
                        String childUsername = doc.getString("username");

                        switch (change.getType()) {

                            case ADDED:
                                addChildButton(childId, childUsername);
                                break;

                            case REMOVED:
                                removeChildButton(childId);
                                break;

                            case MODIFIED:
                                updateChildButton(childId, childUsername);
                                break;
                        }
                    }
                });
    }

    private void addChildButton(String childId, String username) {

        Button childButton = new Button(this);
        childButton.setId(childId.hashCode()); // unique ID
        childButton.setText(username);
        childButton.setTextSize(16);

        childButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, NavigationActivity.class);
            intent.putExtra("childId", childId);
            startActivity(intent);
        });

        containerButtons.addView(childButton);
    }

    private void removeChildButton(String childId) {
        int buttonId = childId.hashCode();
        Button button = containerButtons.findViewById(buttonId);
        if (button != null) {
            containerButtons.removeView(button);
        }
    }

    private void updateChildButton(String childId, String newUsername) {
        int buttonId = childId.hashCode();
        Button button = containerButtons.findViewById(buttonId);
        if (button != null) {
            button.setText(newUsername);
        }
    }
}
