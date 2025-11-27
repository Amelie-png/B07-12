package com.example.demoapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedUserViewModel extends ViewModel {

    private final MutableLiveData<String> userIdLiveData = new MutableLiveData<>();

    // Write userId (childId or parentUid)
    public void setUserId(String userId) {
        userIdLiveData.setValue(userId);
    }

    // Read userId
    public LiveData<String> getUserId() {
        return userIdLiveData;
    }
}
