package com.example.demoapp.login;

public interface LoginContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showError(String message);
        void showSuccess(String message);

        void navigateToChildHome();
        void navigateToParentIdentitySelection();
        void navigateToProviderHome();
    }

    interface Presenter {
        void handleLogin(String identifier, String password);
    }
}

