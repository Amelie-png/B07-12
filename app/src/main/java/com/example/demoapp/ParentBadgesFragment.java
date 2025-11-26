package com.example.demoapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ParentBadgesFragment extends Fragment {

    private String parentUid;

    public ParentBadgesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_badges, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Unified key: always "uid"
        if (getArguments() != null) {
            parentUid = getArguments().getString("uid");   // <-- FIXED
        }

        Log.d("ParentBadgesFragment", "parentUid = " + parentUid);
    }
}
