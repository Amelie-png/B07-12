package com.example.demoapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ChildSymptomsFragment extends Fragment {

    private String uid;
    private String role;

    public ChildSymptomsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_symptoms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Updated to use unified argument key "uid"
        if (getArguments() != null) {
            uid = getArguments().getString("uid");
            role = getArguments().getString("role");
        }

        Log.d("ChildSymptomsFragment", "childUid = " + uid);
        ChildSymptomsContentFragment childAddSymptomScreen = new ChildSymptomsContentFragment();

        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        bundle.putString("role", role);
        childAddSymptomScreen.setArguments(bundle);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, childAddSymptomScreen)
                .commit();
    }
}