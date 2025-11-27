package com.example.demoapp;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ChildMedicineFragment extends Fragment {

    private String childUid;

    public ChildMedicineFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_child_medicine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ---- Retrieve UID from arguments ----
        if (getArguments() != null) {
            childUid = getArguments().getString("uid");
        }

        Log.d("ChildMedicineFragment", "childUid = " + childUid);
    }
}
