package com.example.demoapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ParentHomeFragment extends Fragment {

    private String parentUid;

    public ParentHomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Retrieve UID from unified navigation system
        if (getArguments() != null) {
            parentUid = getArguments().getString("uid");  // <-- Updated key
        }

        Log.d("ParentHomeFragment", "parentUid = " + parentUid);
    }
}