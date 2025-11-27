package com.example.demoapp.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * Repository for handling all Firestore user profile operations.
 * This is the MODEL layer in MVP.
 * No UI logic should appear here.
 */
public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Saves a user's profile data to Firestore.
     *
     * Automatically includes new fields:
     *  - createdAt (timestamp)
     *  - hasSeenOnboarding (default = false)
     */
    public Task<Void> saveUserProfile(String uid, Map<String, Object> data) {
        // Always add creation timestamp
        data.put("createdAt", Timestamp.now());

        // New field for onboarding
        if (!data.containsKey("hasSeenOnboarding")) {
            data.put("hasSeenOnboarding", false);
        }

        return db.collection("users")
                .document(uid)
                .set(data);
    }

    /**
     * Retrieves an entire user document from Firestore.
     */
    public Task<DocumentSnapshot> getUserProfile(String uid) {
        return db.collection("users")
                .document(uid)
                .get();
    }

    /**
     * Retrieves only the user's role from Firestore.
     * Useful for routing after login.
     */
    public Task<DocumentSnapshot> getUserRole(String uid) {
        return db.collection("users")
                .document(uid)
                .get(); // Presenter will extract "role" from the snapshot
    }

    /**
     * Marks a user as having completed onboarding.
     */
    public Task<Void> setOnboardingSeen(String uid) {
        return db.collection("users")
                .document(uid)
                .update("hasSeenOnboarding", true);
    }
}
