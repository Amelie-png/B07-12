package com.example.demoapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Utility class for retrieving the UID (User ID) of the currently authenticated user.
 *
 * <p>This class centralizes access to Firebase Authentication’s current user object.
 * It ensures that all parts of the app retrieve the UID in a consistent manner.</p>
 *
 * <p>If no user is signed in, the method returns a fixed fallback UID, which is useful
 * during development and testing when authentication may not be active.</p>
 *
 * <p><b>Note:</b> This fallback value should be removed or replaced before production
 * deployment to ensure proper authentication flow.</p>
 */
public class UserUtils {

    /**
     * Retrieves the UID of the currently authenticated Firebase user.
     *
     * <p>If a user is logged in, their Firebase-provided UID is returned.
     * If no user is authenticated, the method returns a hardcoded UID
     * for testing purposes (referencing an existing document in Firestore).</p>
     *
     * @return the current user's UID, or a fallback UID for testing scenarios
     */
    public static String getUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Case 1: A user is signed in → use their real Firebase UID.
        if (user != null) {
            return user.getUid();
        }

        // Case 2: No user is logged in → return a test UID.
        // This ensures the app continues running without authentication
        // during development. Must be removed in a real release build.
        return "49ESOyBKI5PcfGN33PxR";
    }
}

