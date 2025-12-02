package com.example.demoapp.login;

/**
 * Contract interface for the Login feature following the MVP (Model–View–Presenter) architecture.
 * <p>
 * This interface defines the responsibilities of both the View and the Presenter.
 * The View handles all UI-related behaviors, while the Presenter handles the
 * business logic and communicates results back to the View.
 */
public interface LoginContract {

    /**
     * Represents the View layer in the MVP architecture for the login process.
     * <p>
     * Any Activity or Fragment that displays the login UI must implement this interface.
     * The View should contain only UI logic—no business or authentication logic.
     */
    interface View {

        /**
         * Displays a loading indicator (e.g., progress bar or spinner)
         * while the login request is being processed.
         */
        void showLoading();

        /**
         * Hides the loading indicator after the login request completes,
         * regardless of whether it succeeded or failed.
         */
        void hideLoading();

        /**
         * Displays an error message to the user if authentication fails
         * or input validation fails.
         *
         * @param message The error message describing what went wrong.
         */
        void showError(String message);

        /**
         * Displays a success message when the user successfully logs in.
         *
         * @param message The success message to display.
         */
        void showSuccess(String message);

        /**
         * Navigates the user to the Child Home screen after successful login
         * as a child user.
         *
         * @param childId The unique identifier of the child account.
         */
        void navigateToChildHome(String childId);

        /**
         * Navigates the user to the Parent Identity Selection screen
         * after a successful login as a parent user.
         *
         * @param parentUid The unique UID of the parent user.
         */
        void navigateToParentIdentitySelection(String parentUid);

        /**
         * Navigates the user to the Provider Home screen after a successful
         * login as a provider user.
         *
         * @param providerUid The unique UID of the provider user.
         */
        void navigateToProviderHome(String providerUid);
    }

    /**
     * Represents the Presenter layer in the MVP architecture for login.
     * <p>
     * The Presenter handles the logic for validating login inputs,
     * performing authentication, and deciding which navigation path
     * the user should follow.
     */
    interface Presenter {

        /**
         * Handles the login request using the provided identifier and password.
         * <p>
         * The identifier may be a username, email, or other account ID depending
         * on the app's login rules.
         *
         * @param identifier The username, email, or ID entered by the user.
         * @param password   The password associated with the account.
         */
        void handleLogin(String identifier, String password);
    }
}
