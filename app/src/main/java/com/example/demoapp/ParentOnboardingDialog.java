package com.example.demoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ParentOnboardingDialog extends DialogFragment {

    public ParentOnboardingDialog() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_parent_onboarding, container, false);

        Button close = view.findViewById(R.id.closeParentPopup);
        close.setOnClickListener(v -> dismiss());

        return view;
    }
}
