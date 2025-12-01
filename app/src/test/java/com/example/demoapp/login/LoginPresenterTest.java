package com.example.demoapp.login;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.demoapp.utils.HashUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.Collections;

@SuppressWarnings("unchecked")
public class LoginPresenterTest {

    @Mock
    private LoginContract.View mockView;

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private Task<AuthResult> mockAuthTask;

    @Mock
    private Task<QuerySnapshot> mockQueryTask;

    @Mock
    private Task<DocumentSnapshot> mockDocTask;

    @Mock
    private AuthResult mockAuthResult;

    @Mock
    private FirebaseUser mockUser;

    @Mock
    private DocumentSnapshot mockUserDoc;

    @Mock
    private QuerySnapshot mockQuerySnapshot;

    private LoginPresenter presenter;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        presenter = new LoginPresenter(mockView, mockAuth, mockFirestore);
    }

    // ---------------------------------------------------------
    // Helper: Make any Task fire addOnCompleteListener immediately
    // ---------------------------------------------------------
    private <T> void triggerComplete(Task<T> task, T result, boolean success) {
        when(task.isSuccessful()).thenReturn(success);
        when(task.getResult()).thenReturn(result);

        doAnswer(inv -> {
            OnCompleteListener<T> listener = inv.getArgument(0);
            listener.onComplete(task);
            return task;
        }).when(task).addOnCompleteListener(any());
    }

    // ---------------------------------------------------------
    // Helper: Make DocumentSnapshot callbacks fire immediately
    // ---------------------------------------------------------
    private void triggerSuccess(Task<DocumentSnapshot> task, DocumentSnapshot doc) {
        doAnswer(inv -> {
            OnSuccessListener<DocumentSnapshot> l = inv.getArgument(0);
            l.onSuccess(doc);
            return task;
        }).when(task).addOnSuccessListener(any());
    }

    // ---------------------------------------------------------
    // TESTS
    // ---------------------------------------------------------

    @Test
    public void testEmptyFieldsShowsError() {
        presenter.handleLogin("", "");
        verify(mockView).showError("Please enter all fields.");
    }

    @Test
    public void testEmailLoginParentSuccess() {

        String email = "parent@test.com";
        String password = "123";
        String uid = "UID_PARENT";

        when(mockAuth.signInWithEmailAndPassword(email, password))
                .thenReturn(mockAuthTask);

        when(mockAuthResult.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        triggerComplete(mockAuthTask, mockAuthResult, true);

        CollectionReference usersCol = mock(CollectionReference.class);
        DocumentReference docRef = mock(DocumentReference.class);

        when(mockFirestore.collection("users")).thenReturn(usersCol);
        when(usersCol.document(uid)).thenReturn(docRef);
        when(docRef.get()).thenReturn(mockDocTask);

        when(mockUserDoc.exists()).thenReturn(true);
        when(mockUserDoc.getString("role")).thenReturn("Parent");

        triggerSuccess(mockDocTask, mockUserDoc);

        presenter.handleLogin(email, password);

        verify(mockView).showLoading();
        verify(mockView).navigateToParentIdentitySelection(uid);
    }

    @Test
    public void testEmailLoginWrongPassword() {

        when(mockAuth.signInWithEmailAndPassword(eq("bad@test.com"), anyString()))
                .thenReturn(mockAuthTask);

        triggerComplete(mockAuthTask, mockAuthResult, false);

        presenter.handleLogin("bad@test.com", "wrongpass");

        verify(mockView).hideLoading();
        verify(mockView).showError("Invalid email or password.");
    }

    @Test
    public void testEmailLoginChildForbidden() {
        String email = "child@test.com";
        String password = "abc";
        String uid = "C123";

        when(mockAuth.signInWithEmailAndPassword(email, password))
                .thenReturn(mockAuthTask);

        when(mockAuthResult.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        triggerComplete(mockAuthTask, mockAuthResult, true);

        CollectionReference usersCol = mock(CollectionReference.class);
        DocumentReference docRef = mock(DocumentReference.class);

        when(mockFirestore.collection("users")).thenReturn(usersCol);
        when(usersCol.document(uid)).thenReturn(docRef);
        when(docRef.get()).thenReturn(mockDocTask);

        when(mockUserDoc.exists()).thenReturn(true);
        when(mockUserDoc.getString("role")).thenReturn("Child");

        triggerSuccess(mockDocTask, mockUserDoc);

        presenter.handleLogin(email, password);

        verify(mockView).showError("Email login is not allowed for Child accounts.");
    }

    @Test
    public void testUsernameLoginChildSuccess() {

        String username = "kid";
        String password = "abc";
        String hashed = HashUtils.sha256(password);
        String childId = "CHILD_UID";

        CollectionReference childrenCol = mock(CollectionReference.class);
        Query childQuery = mock(Query.class);

        when(mockFirestore.collection("children")).thenReturn(childrenCol);
        when(childrenCol.whereEqualTo("username", username)).thenReturn(childQuery);
        when(childQuery.get()).thenReturn(mockQueryTask);

        QueryDocumentSnapshot childDoc = mock(QueryDocumentSnapshot.class);

        when(mockQuerySnapshot.isEmpty()).thenReturn(false);
        when(mockQuerySnapshot.iterator())
                .thenReturn(Collections.singletonList(childDoc).iterator());

        when(childDoc.getString("passwordHash")).thenReturn(hashed);
        when(childDoc.getId()).thenReturn(childId);

        triggerComplete(mockQueryTask, mockQuerySnapshot, true);

        presenter.handleLogin(username, password);

        verify(mockView).navigateToChildHome(childId);
    }

    @Test
    public void testUsernameLoginChildWrongPassword() {

        String username = "kid";

        CollectionReference childrenCol = mock(CollectionReference.class);
        Query childQuery = mock(Query.class);

        when(mockFirestore.collection("children")).thenReturn(childrenCol);
        when(childrenCol.whereEqualTo("username", username)).thenReturn(childQuery);
        when(childQuery.get()).thenReturn(mockQueryTask);

        QueryDocumentSnapshot childDoc = mock(QueryDocumentSnapshot.class);

        when(mockQuerySnapshot.isEmpty()).thenReturn(false);
        when(mockQuerySnapshot.iterator())
                .thenReturn(Collections.singletonList(childDoc).iterator());

        when(childDoc.getString("passwordHash")).thenReturn("WRONG_HASH");

        triggerComplete(mockQueryTask, mockQuerySnapshot, true);

        presenter.handleLogin(username, "abc");

        verify(mockView).hideLoading();
        verify(mockView).showError("Incorrect password.");
    }

    @Test
    public void testUsernameLoginParentSuccess() {

        String username = "parentX";
        String email = "parent@test.com";
        String password = "123";
        String uid = "UID_PARENT";

        // ---------- CHILD QUERY (empty) ----------
        CollectionReference childrenCol = mock(CollectionReference.class);
        Query childQuery = mock(Query.class);
        Task<QuerySnapshot> childTask = mock(Task.class);
        QuerySnapshot emptySnap = mock(QuerySnapshot.class);

        when(mockFirestore.collection("children")).thenReturn(childrenCol);
        when(childrenCol.whereEqualTo("username", username)).thenReturn(childQuery);
        when(childQuery.get()).thenReturn(childTask);

        when(emptySnap.isEmpty()).thenReturn(true);

        triggerComplete(childTask, emptySnap, true);

        // ---------- USER QUERY (found) ----------
        CollectionReference usersCol = mock(CollectionReference.class);
        Query userQuery = mock(Query.class);
        Task<QuerySnapshot> userTask = mock(Task.class);
        QuerySnapshot userSnap = mock(QuerySnapshot.class);
        QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);

        when(mockFirestore.collection("users")).thenReturn(usersCol);
        when(usersCol.whereEqualTo("username", username)).thenReturn(userQuery);
        when(userQuery.get()).thenReturn(userTask);

        when(userSnap.isEmpty()).thenReturn(false);
        when(userSnap.iterator()).thenReturn(Collections.singletonList(userDoc).iterator());

        when(userDoc.getString("email")).thenReturn(email);
        when(userDoc.getString("role")).thenReturn("Parent");

        triggerComplete(userTask, userSnap, true);

        // ---------- AUTH LOGIN ----------
        when(mockAuth.signInWithEmailAndPassword(email, password))
                .thenReturn(mockAuthTask);

        when(mockAuthResult.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        triggerComplete(mockAuthTask, mockAuthResult, true);

        // ---------- TEST ----------
        presenter.handleLogin(username, password);

        verify(mockView).navigateToParentIdentitySelection(uid);
    }

    @Test
    public void testUsernameLogin_ChildQueryFails() {
        String username = "test";

        CollectionReference childrenCol = mock(CollectionReference.class);
        Query childQuery = mock(Query.class);
        Task<QuerySnapshot> childTask = mock(Task.class);

        when(mockFirestore.collection("children")).thenReturn(childrenCol);
        when(childrenCol.whereEqualTo("username", username)).thenReturn(childQuery);
        when(childQuery.get()).thenReturn(childTask);

        triggerComplete(childTask, mockQuerySnapshot, false); // FAIL

        presenter.handleLogin(username, "123");

        verify(mockView).hideLoading();
        verify(mockView).showError("Login failed.");
    }

    @Test
    public void testUsernameLogin_UserQueryFails() {
        String username = "parent1";

        // Child query EMPTY
        CollectionReference childrenCol = mock(CollectionReference.class);
        Query childQuery = mock(Query.class);
        Task<QuerySnapshot> childTask = mock(Task.class);
        QuerySnapshot emptyChild = mock(QuerySnapshot.class);

        when(mockFirestore.collection("children")).thenReturn(childrenCol);
        when(childrenCol.whereEqualTo("username", username)).thenReturn(childQuery);
        when(childQuery.get()).thenReturn(childTask);

        when(emptyChild.isEmpty()).thenReturn(true);
        triggerComplete(childTask, emptyChild, true);

        // User query FAIL
        CollectionReference usersCol = mock(CollectionReference.class);
        Query userQuery = mock(Query.class);
        Task<QuerySnapshot> userTask = mock(Task.class);

        when(mockFirestore.collection("users")).thenReturn(usersCol);
        when(usersCol.whereEqualTo("username", username)).thenReturn(userQuery);
        when(userQuery.get()).thenReturn(userTask);

        triggerComplete(userTask, null, false); // FAIL

        presenter.handleLogin(username, "pass");

        verify(mockView).hideLoading();
        verify(mockView).showError("Login failed.");
    }

    @Test
    public void testUsernameLogin_UserQueryEmpty() {
        String username = "parent1";

        CollectionReference childrenCol = mock(CollectionReference.class);
        when(mockFirestore.collection("children")).thenReturn(childrenCol);

        Query childQuery = mock(Query.class);
        Task<QuerySnapshot> childTask = mock(Task.class);
        QuerySnapshot emptyChild = mock(QuerySnapshot.class);
        when(childrenCol.whereEqualTo("username", username)).thenReturn(childQuery);
        when(childQuery.get()).thenReturn(childTask);

        when(emptyChild.isEmpty()).thenReturn(true);
        triggerComplete(childTask, emptyChild, true);

        CollectionReference usersCol = mock(CollectionReference.class);
        when(mockFirestore.collection("users")).thenReturn(usersCol);

        Query userQuery = mock(Query.class);
        Task<QuerySnapshot> userTask = mock(Task.class);
        QuerySnapshot emptyUser = mock(QuerySnapshot.class);
        when(usersCol.whereEqualTo("username", username)).thenReturn(userQuery);
        when(userQuery.get()).thenReturn(userTask);

        when(emptyUser.isEmpty()).thenReturn(true);
        triggerComplete(userTask, emptyUser, true);

        presenter.handleLogin(username, "123");

        verify(mockView).hideLoading();
        verify(mockView).showError("Invalid username or password.");
    }


    @Test
    public void testUsernameLogin_UserDataMissing() {

        String username = "parent1";

        // ----- MOCK children collection -----
        CollectionReference childrenCol = mock(CollectionReference.class);
        when(mockFirestore.collection("children")).thenReturn(childrenCol);

        Query childQuery = mock(Query.class);
        Task<QuerySnapshot> childTask = mock(Task.class);
        QuerySnapshot emptyChildSnap = mock(QuerySnapshot.class);

        when(childrenCol.whereEqualTo("username", username)).thenReturn(childQuery);
        when(childQuery.get()).thenReturn(childTask);

        when(emptyChildSnap.isEmpty()).thenReturn(true);
        triggerComplete(childTask, emptyChildSnap, true);


        // ----- MOCK users collection -----
        CollectionReference usersCol = mock(CollectionReference.class);
        when(mockFirestore.collection("users")).thenReturn(usersCol);

        Query userQuery = mock(Query.class);
        Task<QuerySnapshot> userTask = mock(Task.class);
        QuerySnapshot userSnap = mock(QuerySnapshot.class);
        QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);

        when(usersCol.whereEqualTo("username", username)).thenReturn(userQuery);
        when(userQuery.get()).thenReturn(userTask);

        when(userSnap.isEmpty()).thenReturn(false);
        when(userSnap.iterator()).thenReturn(Collections.singletonList(userDoc).iterator());

        // HERE: simulate missing email/role
        when(userDoc.getString("email")).thenReturn(null);
        when(userDoc.getString("role")).thenReturn("Parent");

        triggerComplete(userTask, userSnap, true);

        // RUN
        presenter.handleLogin(username, "123");

        // VERIFY
        verify(mockView).hideLoading();
        verify(mockView).showError("User data missing.");
    }


    @Test
    public void testUsernameLogin_ParentAuthFails() {
        String username = "parent1";

        CollectionReference childrenCol = mock(CollectionReference.class);
        when(mockFirestore.collection("children")).thenReturn(childrenCol);

        Query childQuery = mock(Query.class);
        Task<QuerySnapshot> childTask = mock(Task.class);
        QuerySnapshot emptyChild = mock(QuerySnapshot.class);

        when(childrenCol.whereEqualTo("username", username)).thenReturn(childQuery);
        when(childQuery.get()).thenReturn(childTask);

        when(emptyChild.isEmpty()).thenReturn(true);
        triggerComplete(childTask, emptyChild, true);

        CollectionReference usersCol = mock(CollectionReference.class);
        when(mockFirestore.collection("users")).thenReturn(usersCol);

        Query userQuery = mock(Query.class);
        Task<QuerySnapshot> userTask = mock(Task.class);
        QuerySnapshot userSnap = mock(QuerySnapshot.class);
        QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);

        when(usersCol.whereEqualTo("username", username)).thenReturn(userQuery);
        when(userQuery.get()).thenReturn(userTask);

        when(userSnap.isEmpty()).thenReturn(false);
        when(userSnap.iterator()).thenReturn(Collections.singletonList(userDoc).iterator());

        when(userDoc.getString("email")).thenReturn("test@test.com");
        when(userDoc.getString("role")).thenReturn("Parent");

        triggerComplete(userTask, userSnap, true);

        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask);

        triggerComplete(mockAuthTask, mockAuthResult, false);

        presenter.handleLogin(username, "123");

        verify(mockView).hideLoading();
        verify(mockView).showError("Incorrect password.");
    }


    @Test
    public void testUsernameLogin_ProviderSuccess() {

        String username = "provUser";
        String password = "pass123";
        String hashed = HashUtils.sha256(password);
        String uid = "UID_PROVIDER";

        // ---- CHILD QUERY (empty → skip child path) ----
        CollectionReference childrenCol = mock(CollectionReference.class);
        Query childQuery = mock(Query.class);
        Task<QuerySnapshot> childTask = mock(Task.class);
        QuerySnapshot emptyChildSnap = mock(QuerySnapshot.class);

        when(mockFirestore.collection("children")).thenReturn(childrenCol);
        when(childrenCol.whereEqualTo("username", username)).thenReturn(childQuery);
        when(childQuery.get()).thenReturn(childTask);

        when(emptyChildSnap.isEmpty()).thenReturn(true);

        triggerComplete(childTask, emptyChildSnap, true);


        // ---- USER QUERY (provider record found) ----
        CollectionReference usersCol = mock(CollectionReference.class);
        Query userQuery = mock(Query.class);
        Task<QuerySnapshot> userTask = mock(Task.class);
        QuerySnapshot userSnap = mock(QuerySnapshot.class);
        QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);

        when(mockFirestore.collection("users")).thenReturn(usersCol);
        when(usersCol.whereEqualTo("username", username)).thenReturn(userQuery);
        when(userQuery.get()).thenReturn(userTask);

        when(userSnap.isEmpty()).thenReturn(false);
        when(userSnap.iterator()).thenReturn(Collections.singletonList(userDoc).iterator());

        when(userDoc.getString("email")).thenReturn("provider@test.com");
        when(userDoc.getString("role")).thenReturn("Provider");


        triggerComplete(userTask, userSnap, true);


        // ---- AUTH SUCCESS ----
        when(mockAuth.signInWithEmailAndPassword("provider@test.com", password))
                .thenReturn(mockAuthTask);

        when(mockAuthResult.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        triggerComplete(mockAuthTask, mockAuthResult, true);


        // ---- ACT ----
        presenter.handleLogin(username, password);


        // ---- ASSERT ----
        verify(mockView).navigateToProviderHome(uid);
    }



    @Test
    public void testUsernameLogin_UnknownRole() {
        String username = "parent1";

        // ----- MOCK children collection -----
        CollectionReference childrenCol = mock(CollectionReference.class);
        when(mockFirestore.collection("children")).thenReturn(childrenCol);

        Query childQuery = mock(Query.class);
        Task<QuerySnapshot> childTask = mock(Task.class);
        QuerySnapshot emptyChild = mock(QuerySnapshot.class);

        when(childrenCol.whereEqualTo("username", username)).thenReturn(childQuery);
        when(childQuery.get()).thenReturn(childTask);

        when(emptyChild.isEmpty()).thenReturn(true);
        triggerComplete(childTask, emptyChild, true);

        // ----- MOCK users collection -----
        CollectionReference usersCol = mock(CollectionReference.class);
        when(mockFirestore.collection("users")).thenReturn(usersCol);

        Query userQuery = mock(Query.class);
        Task<QuerySnapshot> userTask = mock(Task.class);
        QuerySnapshot userSnap = mock(QuerySnapshot.class);
        QueryDocumentSnapshot userDoc = mock(QueryDocumentSnapshot.class);

        when(usersCol.whereEqualTo("username", username)).thenReturn(userQuery);
        when(userQuery.get()).thenReturn(userTask);

        when(userSnap.isEmpty()).thenReturn(false);
        when(userSnap.iterator()).thenReturn(Collections.singletonList(userDoc).iterator());

        when(userDoc.getString("email")).thenReturn("test@x.com");
        when(userDoc.getString("role")).thenReturn("Alien");

        triggerComplete(userTask, userSnap, true);

        // ----- Auth -----
        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask);

        when(mockAuthResult.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("UID123");

        triggerComplete(mockAuthTask, mockAuthResult, true);

        presenter.handleLogin(username, "pass");

        verify(mockView).showError("Unknown role.");
    }

    @Test
    public void testEmailLogin_UserProfileNotFound() {

        String email = "x@test.com";
        String password = "123";
        String uid = "UID123";

        when(mockAuth.signInWithEmailAndPassword(email, password))
                .thenReturn(mockAuthTask);

        when(mockAuthResult.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        triggerComplete(mockAuthTask, mockAuthResult, true);

        // Firestore
        CollectionReference usersCol = mock(CollectionReference.class);
        DocumentReference docRef = mock(DocumentReference.class);

        when(mockFirestore.collection("users")).thenReturn(usersCol);
        when(usersCol.document(uid)).thenReturn(docRef);
        when(docRef.get()).thenReturn(mockDocTask);

        // doc.exists() = false
        when(mockUserDoc.exists()).thenReturn(false);

        triggerSuccess(mockDocTask, mockUserDoc);

        presenter.handleLogin(email, password);

        verify(mockView).hideLoading();
        verify(mockView).showError("User profile not found.");
    }

    @Test
    public void testEmailLogin_RoleMissing() {

        String email = "x@test.com";
        String password = "123";
        String uid = "UID123";

        when(mockAuth.signInWithEmailAndPassword(email, password))
                .thenReturn(mockAuthTask);

        when(mockAuthResult.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        triggerComplete(mockAuthTask, mockAuthResult, true);

        // Firestore
        CollectionReference usersCol = mock(CollectionReference.class);
        DocumentReference docRef = mock(DocumentReference.class);

        when(mockFirestore.collection("users")).thenReturn(usersCol);
        when(usersCol.document(uid)).thenReturn(docRef);
        when(docRef.get()).thenReturn(mockDocTask);

        when(mockUserDoc.exists()).thenReturn(true);
        when(mockUserDoc.getString("role")).thenReturn(null);

        triggerSuccess(mockDocTask, mockUserDoc);

        presenter.handleLogin(email, password);

        verify(mockView).hideLoading();
        verify(mockView).showError("User role missing.");
    }

    @Test
    public void testEmailLogin_ChildForbidden() {

        String email = "child@test.com";
        String password = "abc";
        String uid = "UID_CHILD";

        when(mockAuth.signInWithEmailAndPassword(email, password))
                .thenReturn(mockAuthTask);

        when(mockAuthResult.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        triggerComplete(mockAuthTask, mockAuthResult, true);

        CollectionReference usersCol = mock(CollectionReference.class);
        DocumentReference docRef = mock(DocumentReference.class);

        when(mockFirestore.collection("users")).thenReturn(usersCol);
        when(usersCol.document(uid)).thenReturn(docRef);
        when(docRef.get()).thenReturn(mockDocTask);

        when(mockUserDoc.exists()).thenReturn(true);
        when(mockUserDoc.getString("role")).thenReturn("Child");

        triggerSuccess(mockDocTask, mockUserDoc);

        presenter.handleLogin(email, password);

        verify(mockView).hideLoading();
        verify(mockView).showError("Email login is not allowed for Child accounts.");
    }

    @Test
    public void testEmailLoginProviderSuccess() {

        String email = "provider@test.com";
        String password = "123";
        String uid = "UID_PROVIDER";

        // Step 1: Mock auth success
        when(mockAuth.signInWithEmailAndPassword(email, password))
                .thenReturn(mockAuthTask);

        when(mockAuthResult.getUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        triggerComplete(mockAuthTask, mockAuthResult, true);

        // Step 2: Mock Firestore user document
        CollectionReference usersCol = mock(CollectionReference.class);
        DocumentReference docRef = mock(DocumentReference.class);

        when(mockFirestore.collection("users")).thenReturn(usersCol);
        when(usersCol.document(uid)).thenReturn(docRef);
        when(docRef.get()).thenReturn(mockDocTask);

        when(mockUserDoc.exists()).thenReturn(true);
        when(mockUserDoc.getString("role")).thenReturn("Provider");

        triggerSuccess(mockDocTask, mockUserDoc);

        // Step 3: Execute login
        presenter.handleLogin(email, password);

        // Step 4: Verification
        verify(mockView).showLoading();
        verify(mockView).hideLoading();
        verify(mockView).navigateToProviderHome(uid);
    }

    @Test(expected = IllegalStateException.class)
    public void testDefaultConstructorThrows() {
        new LoginPresenter(mockView);
    }




}
