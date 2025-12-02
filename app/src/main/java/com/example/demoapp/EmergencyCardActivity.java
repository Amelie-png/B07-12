package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity representing the child's Emergency Action Card screen.
 *
 * <p>This screen provides important emergency information to the user and includes:
 * <ul>
 *     <li>A back button to return to the previous page</li>
 *     <li>(Optional) The ability to notify the parent that the child viewed or triggered emergency actions</li>
 * </ul>
 *
 * <p>This Activity expects two optional extras:
 * <ul>
 *     <li><b>"childId"</b> – Firestore ID of the child</li>
 *     <li><b>"parentId"</b> – Firestore ID of the parent</li>
 * </ul>
 * If parentId is not provided, it is fetched automatically from Firestore.</p>
 */
public class EmergencyCardActivity extends AppCompatActivity {

    /** The Firestore document ID of the child viewing the emergency card. */
    private String childId;

    /** The Firestore document ID of the child’s parent. */
    private String parentId;

    /**
     * Lifecycle method that initializes the Activity, loads IDs from Intent extras,
     * and sets up UI components.
     *
     * @param savedInstanceState Previously saved instance state (unused here).
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_emergency);

        // -------------------------------------------------
        // Retrieve childId / parentId passed from activity
        // -------------------------------------------------
        Intent intent = getIntent();
        childId = intent.getStringExtra("childId");
        parentId = intent.getStringExtra("parentId");

        // If childId is missing, fall back to the currently logged-in user
        if (childId == null) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                childId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        }

        // Load parentId from Firestore if not provided
        if (parentId == null && childId != null) {
            fetchParentIdFromFirestore(childId);
        }

        // ---------------------------
        // Back button (close screen)
        // ---------------------------
        Button backHomeBtn = findViewById(R.id.btnBackHome);

        backHomeBtn.setOnClickListener(v -> {
            finish();  // Simply return to previous screen
        });
    }

    /**
     * Retrieves the parentId for a given child from Firestore.
     *
     * <p>This is needed because the Emergency Card may be launched
     * from different parts of the app, some of which do not pass parentId
     * explicitly.</p>
     *
     * @param childId The Firestore ID of the child whose parentId should be loaded.
     */
    private void fetchParentIdFromFirestore(String childId) {

        FirebaseFirestore.getInstance()
                .collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        parentId = doc.getString("parentId");
                        Log.d("EmergencyCard", "ParentId loaded: " + parentId);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("EmergencyCard",
                                "Failed to load parentId: " + e.getMessage())
                );
    }

    /**
     * Sends an alert to the parent that something related to emergency care occurred.
     *
     * <p>This method is optional and not automatically triggered, but may be used later
     * when integrating emergency alert workflows.</p>
     *
     * @param type    The alert category (e.g., "EMERGENCY", "IMPORTANT", etc.)
     * @param message The message shown to the parent.
     */
    private void sendParentAlert(String type, String message) {

        if (childId == null || parentId == null) {
            Log.w("EmergencyCard", "Cannot send alert (IDs missing)");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Build alert document payload
        Map<String, Object> alert = new HashMap<>();
        alert.put("childId", childId);
        alert.put("parentId", parentId);
        alert.put("type", type);
        alert.put("message", message);
        alert.put("timestamp", System.currentTimeMillis());

        db.collection("alerts")
                .add(alert)
                .addOnSuccessListener(a ->
                        Log.d("EmergencyCard", "Alert sent"))
                .addOnFailureListener(e ->
                        Log.e("EmergencyCard",
                                "Failed alert: " + e.getMessage()));
    }
}
