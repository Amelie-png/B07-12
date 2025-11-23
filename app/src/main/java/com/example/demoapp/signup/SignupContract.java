package com.example.demoapp.signup;

public interface SignupContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showError(String message);
        void showSuccess(String message);
    }

    interface Presenter {
        void handleSignup(
                String firstName,
                String lastName,
                String username,
                String email,
                String password,
                String confirmPassword,
                String role   // ← THIS WAS MISSING
        );
    }
}
