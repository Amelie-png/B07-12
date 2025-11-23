package com.example.demoapp.signup;

import androidx.annotation.NonNull;

import com.example.demoapp.data.AuthRepository;
import com.example.demoapp.data.UserRepository;
import com.google.firebase.auth.AuthResult;

import java.util.HashMap;
import java.util.Map;

public class SignupPresenter implements SignupContract.Presenter {

    private final SignupContract.View view;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    public SignupPresenter(SignupContract.View view) {
        this.view = view;
        this.authRepository = new AuthRepository();
        this.userRepository = new UserRepository();
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

        // ================================
        // 1. VALIDATE USER INPUT
        // ================================
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

        view.showLoading();

        // ================================
        // 2. CREATE USER IN FIREBASE AUTH
        // ================================
        authRepository.signup(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        view.hideLoading();
                        view.showError(task.getException().getMessage());
                        return;
                    }

                    String uid = task.getResult().getUser().getUid();

                    // ================================
                    // 3. SAVE USER PROFILE IN FIRESTORE
                    // ================================
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
                });
    }
}
