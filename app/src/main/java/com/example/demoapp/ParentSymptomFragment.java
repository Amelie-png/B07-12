package com.example.demoapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ParentSymptomFragment extends Fragment {

    private String parentUid;

    public ParentSymptomFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_symptom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Updated to use unified argument key "uid"
        if (getArguments() != null) {
            parentUid = getArguments().getString("uid");
        }

        Log.d("ParentSymptomFragment", "parentUid = " + parentUid);
    }
}
