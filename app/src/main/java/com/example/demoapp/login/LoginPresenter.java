package com.example.demoapp.login;

import com.example.demoapp.utils.HashUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginPresenter implements LoginContract.Presenter {

    private final LoginContract.View view;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public LoginPresenter(LoginContract.View view) {
        this(view, FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());
    }

    public LoginPresenter(LoginContract.View view,
                          FirebaseAuth firebaseAuth,
                          FirebaseFirestore firestore) {
        this.view = view;
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }

    @Override
    public void handleLogin(String identifier, String password) {

        if (identifier.isEmpty() || password.isEmpty()) {
            view.showError("Please enter all fields.");
            return;
        }

        view.showLoading();

        if (identifier.contains("@")) {
            loginWithEmail(identifier, password);
        } else {
            loginWithUsername(identifier, password);
        }
    }


    // EMAIL LOGIN
    private void loginWithEmail(String email, String password) {

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {

                    if (!authTask.isSuccessful()) {
                        view.hideLoading();
                        view.showError("Invalid email or password.");
                        return;
                    }

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


    // USERNAME LOGIN
    private void loginWithUsername(String username, String password) {

        firestore.collection("children")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(childTask -> {

                    if (!childTask.isSuccessful()) {
                        view.hideLoading();
                        view.showError("Login failed.");
                        return;
                    }

                    // CHILD FOUND
                    if (!childTask.getResult().isEmpty()) {

                        for (QueryDocumentSnapshot childDoc : childTask.getResult()) {

                            String storedHash = childDoc.getString("passwordHash");
                            String inputHash = HashUtils.sha256(password);

                            if (storedHash == null || !storedHash.equals(inputHash)) {
                                view.hideLoading();
                                view.showError("Incorrect password.");
                                return;
                            }

                            String childId = childDoc.getId();

                            view.hideLoading();
                            view.navigateToChildHome(childId);
                            return;
                        }
                    }

                    // PARENT/PROVIDER
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
                                    view.hideLoading();
                                    view.showError("Invalid username or password.");
                                    return;
                                }

                                for (QueryDocumentSnapshot userDoc : userTask.getResult()) {

                                    String email = userDoc.getString("email");
                                    String role = userDoc.getString("role");

                                    if (email == null || role == null) {
                                        view.hideLoading();
                                        view.showError("User data missing.");
                                        return;
                                    }

                                    firebaseAuth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(authTask -> {

                                                if (!authTask.isSuccessful()) {
                                                    view.hideLoading();
                                                    view.showError("Incorrect password.");
                                                    return;
                                                }

                                                String uid = authTask.getResult().getUser().getUid();

                                                view.hideLoading();

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
