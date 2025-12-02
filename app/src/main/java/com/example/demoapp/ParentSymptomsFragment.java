package com.example.demoapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ParentSymptomsFragment extends Fragment {

    private String parentUid;
    private String childUid;
    private String role;

    public ParentSymptomsFragment() {
    }

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
        Bundle b= getArguments();
        if (getArguments() != null) {
            parentUid = getArguments().getString("parentUid");
            role = getArguments().getString("role");
            childUid = getArguments().getString("uid");
        }

        Log.d("ParentSymptomFragment", "parentUid = " + parentUid);
        ParentSymptomContentFragment parentAddSymptomScreen = new ParentSymptomContentFragment();

        Bundle bundle = new Bundle();
        bundle.putString("uid", parentUid);
        bundle.putString("role", role);
        bundle.putString("childUid", childUid);
        parentAddSymptomScreen.setArguments(bundle);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, parentAddSymptomScreen)
                .commit();
    }
}