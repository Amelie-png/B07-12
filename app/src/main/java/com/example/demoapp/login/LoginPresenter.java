package com.example.demoapp.login;

import com.example.demoapp.utils.HashUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Presenter class for handling login logic in the Login MVP architecture.
 * <p>
 * This class performs:
 * - Input validation
 * - Authentication with Firebase Authentication
 * - Fetching user profiles from Firestore
 * - Determining navigation based on user role (Child, Parent, Provider)
 * <p>
 * The Presenter never interacts with UI components directly. Instead, it
 * communicates back to the View using the {@link LoginContract.View} interface.
 */
public class LoginPresenter implements LoginContract.Presenter {

    /** The view that receives login results and updates the UI. */
    private final LoginContract.View view;

    /** Firebase Authentication instance for email/password login. */
    private final FirebaseAuth firebaseAuth;

    /** Firestore instance for looking up user profiles and roles. */
    private final FirebaseFirestore firestore;

    /**
     * Primary constructor used in production.
     * Initializes FirebaseAuth and FirebaseFirestore automatically.
     *
     * @param view The View interface that displays UI updates for login.
     */
    public LoginPresenter(LoginContract.View view) {
        this(view, FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());
    }

    /**
     * Secondary constructor used mainly for unit testing,
     * allowing dependency injection of mocked Firebase instances.
     *
     * @param view          The view interface used to update UI.
     * @param firebaseAuth  Custom FirebaseAuth instance (mock or real).
     * @param firestore     Custom Firestore instance (mock or real).
     */
    public LoginPresenter(LoginContract.View view,
                          FirebaseAuth firebaseAuth,
                          FirebaseFirestore firestore) {

        this.view = view;
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }

    /**
     * Handles the login process by determining which login method to call.
     * <p>
     * Logic:
     * - Validates inputs
     * - Shows a loading indicator
     * - If identifier contains "@", treat it as an email
     * - Otherwise, treat it as a username
     *
     * @param identifier The email or username entered by the user.
     * @param password   The password entered by the user.
     */
    @Override
    public void handleLogin(String identifier, String password) {

        // Basic input validation
        if (identifier.isEmpty() || password.isEmpty()) {
            view.showError("Please enter all fields.");
            return;
        }

        view.showLoading();

        // Decide which authentication method to use
        if (identifier.contains("@")) {
            loginWithEmail(identifier, password);
        } else {
            loginWithUsername(identifier, password);
        }
    }


    /**
     * Handles login using Firebase Authentication with email and password.
     * <p>
     * Steps:
     * 1. Authenticate with Firebase Auth
     * 2. Retrieve user document from "users" collection
     * 3. Determine role (Parent / Provider)
     * 4. Navigate View accordingly
     *
     * @param email    The user's email.
     * @param password The user's password.
     */
    private void loginWithEmail(String email, String password) {

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {

                    // Authentication failed
                    if (!authTask.isSuccessful()) {
                        view.hideLoading();
                        view.showError("Invalid email or password.");
                        return;
                    }

                    // Fetch UID of authenticated user
                    String uid = authTask.getResult().getUser().getUid();

                    firestore.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {

                                view.hideLoading();

                                if (!doc.exists()) {
                                    view.showError("User profile not found.");
                                    return;
                                }

                                String role = doc.getString("role");

                                if (role == null) {
                                    view.showError("User role missing.");
                                    return;
                                }

                                // Navigate depending on role
                                switch (role) {

                                    case "Parent":
                                        view.navigateToParentIdentitySelection(uid);
                                        break;

                                    case "Provider":
                                        view.navigateToProviderHome(uid);
                                        break;

                                    default:
                                        // Email login should NOT be used for Child accounts
                                        view.showError("Email login is not allowed for Child accounts.");
                                }
                            });
                });
    }


    /**
     * Handles login using a username instead of email.
     * <p>
     * Logic:
     * 1. Check if username belongs to a Child in "children" collection
     * 2. If found:
     *      - Compare hashed password
     *      - Navigate to Child Home
     * 3. If not a child:
     *      - Look for username in "users" (Parents & Providers)
     *      - Authenticate via email/password on FirebaseAuth
     *      - Navigate based on role
     *
     * @param username The entered username.
     * @param password The entered password.
     */
    private void loginWithUsername(String username, String password) {

        // First search the "children" collection
        firestore.collection("children")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(childTask -> {

                    if (!childTask.isSuccessful()) {
                        view.hideLoading();
                        view.showError("Login failed.");
                        return;
                    }

                    // CHILD ACCOUNT FOUND
                    if (!childTask.getResult().isEmpty()) {

                        for (QueryDocumentSnapshot childDoc : childTask.getResult()) {

                            String storedHash = childDoc.getString("passwordHash");
                            String inputHash = HashUtils.sha256(password);

                            // Validate hashed password
                            if (storedHash == null || !storedHash.equals(inputHash)) {
                                view.hideLoading();
                                view.showError("Incorrect password.");
                                return;
                            }

                            // Login successful → navigate to Child home
                            String childId = childDoc.getId();

                            view.hideLoading();
                            view.navigateToChildHome(childId);
                            return;
                        }
                    }

                    // If not a child, try parent/provider accounts
                    firestore.collection("users")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnCompleteListener(userTask -> {

                                if (!userTask.isSuccessful()) {
                                    view.hideLoading();
                                    view.showError("Login failed.");
                                    return;
                                }

                                // No matching username found
                                if (userTask.getResult().isEmpty()) {
                                    view.hideLoading();
                                    view.showError("Invalid username or password.");
                                    return;
                                }

                                // Parent / Provider
                                for (QueryDocumentSnapshot userDoc : userTask.getResult()) {

                                    String email = userDoc.getString("email");
                                    String role = userDoc.getString("role");

                                    if (email == null || role == null) {
                                        view.hideLoading();
                                        view.showError("User data missing.");
                                        return;
                                    }

                                    // Authenticate with Firebase using stored email
                                    firebaseAuth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(authTask -> {

                                                if (!authTask.isSuccessful()) {
                                                    view.hideLoading();
                                                    view.showError("Incorrect password.");
                                                    return;
                                                }

                                                String uid = authTask.getResult().getUser().getUid();

                                                view.hideLoading();

                                                // Navigate based on user role
                                                switch (role) {

                                                    case "Parent":
                                                        view.navigateToParentIdentitySelection(uid);
                                                        break;

                                                    case "Provider":
                                                        view.navigateToProviderHome(uid);
                                                        break;

                                                    default:
                                                        view.showError("Unknown role.");
                                                }
                                            });
                                }
                            });
                });
    }
}
