package com.example.demoapp.login;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginPresenter implements LoginContract.Presenter {

    private final LoginContract.View view;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public LoginPresenter(LoginContract.View view) {
        this.view = view;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void handleLogin(String identifier, String password) {

        if (identifier.isEmpty() || password.isEmpty()) {
            view.showError("Please enter all fields.");
            return;
        }

        view.showLoading();

        if (identifier.contains("@")) {
            // ==================================
            // EMAIL LOGIN (Parent or Provider)
            // ==================================
            loginWithEmail(identifier, password);

        } else {
            // ==================================
            // USERNAME LOGIN (Child, Parent, Provider)
            // ==================================
            loginWithUsername(identifier, password);
        }
    }

    // -------------------------------------------------------
    // EMAIL LOGIN → Parent or Provider
    // -------------------------------------------------------
    private void loginWithEmail(String email, String password) {

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {

                    if (!authTask.isSuccessful()) {
                        view.hideLoading();
                        view.showError("Invalid email or password.");
                        return;
                    }

                    String uid = authTask.getResult().getUser().getUid();

                    // Fetch the user's role from Firestore
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

                                switch (role) {

                                    case "Parent":
                                        view.navigateToParentIdentitySelection(uid);
                                        break;

                                    case "Provider":
                                        view.navigateToProviderHome(uid);
                                        break;

                                    default:
                                        view.showError("Email login is not allowed for Child accounts.");
                                }
                            });
                });
    }

    // -------------------------------------------------------
    // USERNAME LOGIN → Child OR Parent OR Provider
    // -------------------------------------------------------
    private void loginWithUsername(String username, String password) {

        // 1️⃣ FIRST: SEARCH IN CHILDREN COLLECTION
        firestore.collection("children")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(childTask -> {

                    if (!childTask.isSuccessful()) {
                        view.hideLoading();
                        view.showError("Login failed.");
                        return;
                    }

                    if (!childTask.getResult().isEmpty()) {

                        // 🔵 CHILD USER FOUND
                        for (QueryDocumentSnapshot childDoc : childTask.getResult()) {

                            String storedPassword = childDoc.getString("password");

                            if (!password.equals(storedPassword)) {
                                view.hideLoading();
                                view.showError("Incorrect password.");
                                return;
                            }

                            // SUCCESS → Child logged in
                            String childId = childDoc.getId();

                            view.hideLoading();
                            view.navigateToChildHome(childId);
                            return;
                        }
                    }

                    // 2️⃣ IF NOT A CHILD, CHECK PARENT/PROVIDER IN USERS COLLECTION
                    firestore.collection("users")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnCompleteListener(userTask -> {

                                if (!userTask.isSuccessful()) {
                                    view.hideLoading();
                                    view.showError("Login failed.");
                                    return;
                                }

                                if (userTask.getResult().isEmpty()) {
                                    // No match in either collection → invalid username
                                    view.hideLoading();
                                    view.showError("Invalid username or password.");
                                    return;
                                }

                                // 🔵 USER FOUND → Parent or Provider
                                for (QueryDocumentSnapshot userDoc : userTask.getResult()) {

                                    String email = userDoc.getString("email");
                                    String role = userDoc.getString("role");

                                    if (email == null || role == null) {
                                        view.hideLoading();
                                        view.showError("User data missing.");
                                        return;
                                    }

                                    // 3️⃣ Username login → convert username login into EMAIL login
                                    firebaseAuth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(authTask -> {

                                                view.hideLoading();

                                                if (!authTask.isSuccessful()) {
                                                    view.showError("Incorrect password.");
                                                    return;
                                                }

                                                String uid = firebaseAuth.getCurrentUser().getUid();

                                                // SUCCESS → Navigate based on role
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
