package com.example.demoapp.signup;

/**
 * Contract interface for the Signup feature following the MVP (Model–View–Presenter)
 * architectural pattern.
 * <p>
 * This contract defines the responsibilities of both the View and the Presenter.
 * The View handles UI-related behavior (showing loading states, error messages, etc.),
 * while the Presenter handles all signup logic, input validation, and interaction
 * with backend services such as Firebase.
 */
public interface SignupContract {

    /**
     * Represents the View layer in the Signup MVP structure.
     * <p>
     * Any Activity or Fragment responsible for displaying the signup UI
     * must implement this interface. The View should only handle visual updates
     * and must not contain any business logic.
     */
    interface View {

        /**
         * Shows a loading indicator (e.g., progress bar or spinner)
         * while the signup request is being processed.
         */
        void showLoading();

        /**
         * Hides the loading indicator after the signup operation finishes,
         * regardless of success or failure.
         */
        void hideLoading();

        /**
         * Displays an error message to inform the user about a failed signup attempt.
         * This may include validation errors or backend errors.
         *
         * @param message The error message describing the failure.
         */
        void showError(String message);

        /**
         * Displays a success message when the signup process completes successfully.
         *
         * @param message The message confirming that the user was registered successfully.
         */
        void showSuccess(String message);
    }

    /**
     * Represents the Presenter layer in the Signup MVP structure.
     * <p>
     * The Presenter contains all business logic and input validation for user signup.
     * It interacts with the database or backend services and reports results
     * back to the View. It must not reference any Android UI components directly.
     */
    interface Presenter {

        /**
         * Handles the user signup request using the provided form data.
         * <p>
         * Typical responsibilities:
         * <ul>
         *     <li>Validate required fields (non-empty, formatted correctly)</li>
         *     <li>Validate password/confirmPassword match</li>
         *     <li>Check uniqueness for username/email if required</li>
         *     <li>Create the new account and store user details</li>
         *     <li>Trigger appropriate View navigation or success messages</li>
         * </ul>
         *
         * @param firstName       The user's first name.
         * @param lastName        The user's last name.
         * @param username        The desired username chosen by the user.
         * @param email           The user's email address.
         * @param password        The chosen password for authentication.
         * @param confirmPassword The repeated password entered for verification.
         * @param role            The account role (e.g., "Parent", "Provider").
         */
        void handleSignup(String firstName,
                          String lastName,
                          String username,
                          String email,
                          String password,
                          String confirmPassword,
                          String role);
    }
}
