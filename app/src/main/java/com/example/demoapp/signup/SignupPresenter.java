package com.example.demoapp.signup;

import com.example.demoapp.data.AuthRepository;
import com.example.demoapp.data.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupPresenter implements SignupContract.Presenter {

    private final SignupContract.View view;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final FirebaseFirestore firestore;

    public SignupPresenter(SignupContract.View view) {
        this.view = view;
        this.authRepository = new AuthRepository();
        this.userRepository = new UserRepository();
        this.firestore = FirebaseFirestore.getInstance();
    }

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

        // ========================================
        // 1. VALIDATE BASIC INPUT
        // ========================================
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {

            view.showError("Please fill out all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            view.showError("Passwords do not match.");
            return;
        }

        if (role.equals("Select Role") || role.equals("Item 1")) {
            view.showError("Please choose a valid role.");
            return;
        }

        // ========================================
        // 2. VALIDATE USERNAME FORMAT
        // ========================================
        if (!username.matches("[a-zA-Z0-9._-]+")) {
            view.showError("Username may only contain letters, numbers, dots, underscores, and hyphens.");
            return;
        }

        // Disallow email-looking usernames
        if (username.contains("@")) {
            view.showError("Username cannot contain '@'.");
            return;
        }

        view.showLoading();

        // ========================================
        // 3. CHECK USERNAME UNIQUENESS
        // Search both users AND children collections
        // ========================================
        checkUsernameUnique(firstName, lastName, username, email, password, role);
    }


    /**
     * Checks username uniqueness across:
     *  - users collection (Parents + Providers)
     *  - children collection (Child accounts)
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
                        view.showError("Error checking username.");
                        return;
                    }

                    if (!userTask.getResult().isEmpty()) {
                        view.hideLoading();
                        view.showError("Username already exists. Please choose another.");
                        return;
                    }

                    // If not in users, check children
                    firestore.collection("children")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnCompleteListener(childTask -> {

                                if (!childTask.isSuccessful()) {
                                    view.hideLoading();
                                    view.showError("Error checking username.");
                                    return;
                                }

                                if (!childTask.getResult().isEmpty()) {
                                    view.hideLoading();
                                    view.showError("Username already exists. Please choose another.");
                                    return;
                                }

                                // Username is FREE → proceed with FirebaseAuth signup
                                createFirebaseUser(
                                        firstName, lastName, username, email, password, role
                                );

                            });
                });
    }


    /**
     * Creates Firebase Authentication user (email/password)
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

                    String uid = task.getResult().getUser().getUid();

                    saveUserToFirestore(
                            uid, firstName, lastName, username, email, role
                    );
                });
    }


    /**
     * Saves parent/provider data to Firestore
     */
    private void saveUserToFirestore(
            String uid,
            String firstName,
            String lastName,
            String username,
            String email,
            String role
    ) {

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("username", username);
        userData.put("email", email);
        userData.put("role", role);

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
