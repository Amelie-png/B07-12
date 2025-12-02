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

/**
 * Activity that allows a logged-in parent user to choose which identity
 * they want to continue as:
 *
 * <ul>
 *     <li><b>Themselves (Parent Home)</b></li>
 *     <li><b>One of their linked child accounts</b></li>
 * </ul>
 *
 * <p>This screen is shown immediately after a parent logs in.
 * The activity listens to Firestore in real-time, dynamically generating
 * buttons for each child belonging to the parent.</p>
 */
public class ChooseIdentityActivity extends AppCompatActivity {

    /** UID of the logged-in parent, passed from Login or a previous screen. */
    private String parentUid;

    /** Container that will hold the Parent button + dynamically added Child buttons. */
    private LinearLayout containerButtons;

    /** Button for logging in as the parent user. */
    private Button parentHomeButton;

    /**
     * Initializes the activity, binds UI components, retrieves the parent UID,
     * and starts listening to Firestore for associated child accounts.
     *
     * @param savedInstanceState Previously saved state (unused).
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_identity);

        // -----------------------------------------
        // 1. Retrieve parent UID from Intent extras
        // -----------------------------------------
        parentUid = getIntent().getStringExtra("parentUid");

        if (parentUid == null) {
            Toast.makeText(this, "Error: missing parent ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ---------------------
        // 2. Bind UI elements
        // ---------------------
        containerButtons = findViewById(R.id.container_identity_buttons);
        parentHomeButton = findViewById(R.id.button_parent_home);

        // ---------------------------------------------------------
        // Parent button → Navigates to ParentActivity (parent home)
        // ---------------------------------------------------------
        parentHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseIdentityActivity.this, ParentActivity.class);
            intent.putExtra("uid", parentUid);
            intent.putExtra("role", "parent");
            startActivity(intent);
            finish();
        });

        // ---------------------------------------------------------
        // 3. Begin real-time listening for children under this parent
        // ---------------------------------------------------------
        listenToChildAccountsRealtime();
    }

    /**
     * Sets up a real-time Firestore listener that loads all child accounts
     * associated with the current parent (children where "parentId" = parentUid).
     *
     * <p>Whenever the Firestore data changes (child added/removed/renamed),
     * the UI will update instantly, regenerating the child buttons.</p>
     *
     * <p>This method also ensures that only the dynamically created child buttons
     * are removed and regenerated, leaving the parent button unchanged.</p>
     */
    private void listenToChildAccountsRealtime() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("children")
                .whereEqualTo("parentId", parentUid)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Toast.makeText(this, "Error loading children", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // -----------------------------------------------------
                    // Remove all child buttons (keep index 0: parent button)
                    // -----------------------------------------------------
                    int count = containerButtons.getChildCount();
                    if (count > 1) {
                        containerButtons.removeViews(1, count - 1);
                    }

                    if (value == null || value.isEmpty()) {
                        return;
                    }

                    // -----------------------------------------------------
                    // Loop through children and generate a button for each
                    // -----------------------------------------------------
                    for (DocumentSnapshot childDoc : value) {

                        String childName = childDoc.getString("username");
                        String childId = childDoc.getId();

                        // Create a new button for this child
                        Button childButton = new Button(this);
                        childButton.setText("Log in as " + childName);

                        // Add spacing between buttons
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 20, 0, 20);
                        childButton.setLayoutParams(params);

                        // Handle clicking the child button
                        childButton.setOnClickListener(v -> {
                            Intent intent = new Intent(
                                    ChooseIdentityActivity.this,
                                    MainNavActivity.class
                            );
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
