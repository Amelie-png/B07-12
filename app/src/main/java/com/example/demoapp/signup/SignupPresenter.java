package com.example.demoapp.signup;

import com.example.demoapp.data.AuthRepository;
import com.example.demoapp.data.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Presenter responsible for handling all signup-related business logic
 * under the MVP (Model–View–Presenter) architecture.
 *
 * <p>This class does NOT interact with Android UI components directly.
 * Instead, it communicates with the View through the {@link SignupContract.View}
 * interface and delegates authentication + Firestore operations to
 * the repositories.</p>
 *
 * Responsibilities include:
 * <ul>
 *     <li>Input validation for signup fields</li>
 *     <li>Checking username uniqueness across multiple collections</li>
 *     <li>Creating a Firebase Authentication account</li>
 *     <li>Storing user information in Firestore</li>
 * </ul>
 */
public class SignupPresenter implements SignupContract.Presenter {

    /** Reference to the signup View used to update UI states. */
    private final SignupContract.View view;

    /** Repository wrapper for Firebase Authentication. */
    private final AuthRepository authRepository;

    /** Repository wrapper for saving profile data to Firestore. */
    private final UserRepository userRepository;

    /** Firestore database instance used for username checks. */
    private final FirebaseFirestore firestore;

    /**
     * Primary constructor used in production.
     * Initializes repositories and Firestore automatically.
     *
     * @param view The View interface responsible for UI updates.
     */
    public SignupPresenter(SignupContract.View view) {
        this.view = view;
        this.authRepository = new AuthRepository();
        this.userRepository = new UserRepository();
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Handles the full signup procedure, including:
     * <ol>
     *     <li>Checking for empty fields</li>
     *     <li>Validating password + confirmPassword match</li>
     *     <li>Ensuring a valid role is selected</li>
     *     <li>Validating username formatting rules</li>
     *     <li>Ensuring username uniqueness across collections</li>
     * </ol>
     *
     * <p>If validation succeeds, the method proceeds to username-uniqueness checking,
     * and eventually account creation.</p>
     *
     * @param firstName       User's first name
     * @param lastName        User's last name
     * @param username        Desired username
     * @param email           User's email
     * @param password        Password entered
     * @param confirmPassword Password confirmation
     * @param role            Account role ("Parent", "Provider")
     */
    @Override
    public void handleSignup(
            String firstName,
            String lastName,
            String username,
            String email,
            String password,
            String confirmPassword,
            String role
    ) {

        // 1. BASIC FIELD VALIDATION
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {

            view.showError("Please fill out all fields.");
            return;
        }

        // Password match validation
        if (!password.equals(confirmPassword)) {
            view.showError("Passwords do not match.");
            return;
        }

        // 2. ROLE VALIDATION
        if (role.equals("Select account type…")) {
            view.showError("Please choose a valid role.");
            return;
        }

        if (!role.equals("Parent") && !role.equals("Provider")) {
            view.showError("Invalid role.");
            return;
        }

        // 3. USERNAME FORMAT VALIDATION
        if (!username.matches("[a-zA-Z0-9._-]+")) {
            view.showError("Username may only contain letters, numbers, dots, underscores, and hyphens.");
            return;
        }

        // Disallow email-like usernames
        if (username.contains("@")) {
            view.showError("Username cannot contain '@'.");
            return;
        }

        view.showLoading();

        // 4. CHECK USERNAME UNIQUENESS
        checkUsernameUnique(firstName, lastName, username, email, password, role);
    }

    /**
     * Checks whether a username already exists in either the "users" (Parent/Provider) or
     * "children" collections. If the username is available, proceeds with account creation.
     *
     * @param firstName User first name
     * @param lastName  User last name
     * @param username  Desired username
     * @param email     Email address
     * @param password  Password
     * @param role      Account role
     */
    private void checkUsernameUnique(
            String firstName,
            String lastName,
            String username,
            String email,
            String password,
            String role
    ) {

        firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(userTask -> {

                    if (!userTask.isSuccessful()) {
                        view.hideLoading();
                        Exception e = userTask.getException();
                        view.showError("Error checking username: " + (e != null ? e.getMessage() : "Unknown error"));
                        return;
                    }

                    // Username already taken by Parent/Provider
                    if (!userTask.getResult().isEmpty()) {
                        view.hideLoading();
                        view.showError("Username already exists. Please choose another.");
                        return;
                    }

                    // Check CHILDREN collection next
                    firestore.collection("children")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnCompleteListener(childTask -> {

                                if (!childTask.isSuccessful()) {
                                    view.hideLoading();
                                    view.showError("Error checking username.");
                                    return;
                                }

                                // Username taken by a child
                                if (!childTask.getResult().isEmpty()) {
                                    view.hideLoading();
                                    view.showError("Username already exists. Please choose another.");
                                    return;
                                }

                                // Username is AVAILABLE
                                createFirebaseUser(firstName, lastName, username, email, password, role);
                            });
                });
    }

    /**
     * Creates a new Firebase Authentication user using the provided email and password.
     * On success, proceeds to storing the user's profile in Firestore.
     *
     * @param firstName User's first name
     * @param lastName  User's last name
     * @param username  Desired username
     * @param email     Email address
     * @param password  Password for authentication
     * @param role      Account role ("Parent" or "Provider")
     */
    private void createFirebaseUser(
            String firstName,
            String lastName,
            String username,
            String email,
            String password,
            String role
    ) {

        authRepository.signup(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        view.hideLoading();
                        view.showError(task.getException().getMessage());
                        return;
                    }

                    // Obtain UID from the newly created Firebase Auth account
                    String uid = task.getResult().getUser().getUid();
                    saveUserToFirestore(uid, firstName, lastName, username, email, role);
                });
    }

    /**
     * Saves the newly created user's profile information into Firestore.
     * This creates a user record under the "users" collection.
     *
     * @param uid       The Firebase Auth UID associated with the new account
     * @param firstName User's first name
     * @param lastName  User's last name
     * @param username  Username chosen by the user
     * @param email     Email address
     * @param role      Account role
     */
    private void saveUserToFirestore(
            String uid,
            String firstName,
            String lastName,
            String username,
            String email,
            String role
    ) {

        // Build document payload
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("username", username);
        userData.put("email", email);
        userData.put("role", role);
        userData.put("hasSeenOnboarding", false);

        // Save data using repository abstraction
        userRepository.saveUserProfile(uid, userData)
                .addOnCompleteListener(saveTask -> {

                    view.hideLoading();

                    if (saveTask.isSuccessful()) {
                        view.showSuccess("Account created successfully!");
                    } else {
                        view.showError("Failed to save user information.");
                    }
                });
    }
}
