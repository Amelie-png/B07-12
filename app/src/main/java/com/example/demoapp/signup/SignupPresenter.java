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

        // ========================================
        // 2. VALIDATE ROLE
        // ========================================
        // Based on your strings.xml (Select account type…)
        if (role.equals("Select account type…")) {
            view.showError("Please choose a valid role.");
            return;
        }

        // Only Parent + Provider allowed
        if (!role.equals("Parent") && !role.equals("Provider")) {
            view.showError("Invalid role.");
            return;
        }

        // ========================================
        // 3. VALIDATE USERNAME FORMAT
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
        // 4. CHECK USERNAME UNIQUENESS IN BOTH COLLECTIONS
        // ========================================
        checkUsernameUnique(firstName, lastName, username, email, password, role);
    }


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
                        view.showError("Error checking username: " + e.getMessage());
                        return;
                    }


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

                                // Username is FREE!
                                createFirebaseUser(firstName, lastName, username, email, password, role);
                            });
                });
    }


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
                    saveUserToFirestore(uid, firstName, lastName, username, email, role);
                });
    }


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
        userData.put("lastName", lastName); // FIXED typo
        userData.put("username", username);
        userData.put("email", email);
        userData.put("role", role);
        userData.put("hasSeenOnboarding", false);

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
