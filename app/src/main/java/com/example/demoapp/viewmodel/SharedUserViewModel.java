package com.example.demoapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * A shared ViewModel used to store and share the currently active user ID
 * (e.g., childId, parentUid, providerUid) across multiple Activities or Fragments.
 *
 * <p>This ViewModel enables:
 * <ul>
 *     <li>Decoupling Activities/Fragments from direct data passing</li>
 *     <li>Persistence of small pieces of UI-related state across configuration changes</li>
 *     <li>Reactive updates through {@link LiveData}</li>
 * </ul>
 *
 * <p>The user ID stored here is typically set after login or identity selection,
 * and retrieved by various screens that need the authenticated user's information.</p>
 */
public class SharedUserViewModel extends ViewModel {

    /**
     * LiveData container holding the current user's ID.
     * <p>
     * This is exposed as immutable {@link LiveData} to observers, while the
     * {@link MutableLiveData} reference remains private to ensure safe updates.
     */
    private final MutableLiveData<String> userIdLiveData = new MutableLiveData<>();

    /**
     * Updates the current user ID stored in the shared ViewModel.
     * This method is typically called after a successful login or identity switch.
     *
     * @param userId The unique identifier for the logged-in user (child, parent, or provider).
     */
    public void setUserId(String userId) {
        userIdLiveData.setValue(userId);
    }

    /**
     * Returns a read-only {@link LiveData} instance that observers can use to react
     * to changes in the stored user ID.
     *
     * <p>Any Activity or Fragment can observe this LiveData to receive the current user ID
     * without needing to pass data manually through Intent extras.</p>
     *
     * @return Immutable LiveData containing the stored user ID.
     */
    public LiveData<String> getUserId() {
        return userIdLiveData;
    }
}

