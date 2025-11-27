package com.example.demoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ProviderOnboardingDialog extends DialogFragment {

    public ProviderOnboardingDialog() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_provider_onboarding, container, false);

        Button close = view.findViewById(R.id.closeProviderPopup);
        close.setOnClickListener(v -> dismiss());

        return view;
    }
}

