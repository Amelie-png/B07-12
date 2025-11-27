package com.example.demoapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ProviderPasswordRecovery extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provider_password_recovery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button back = view.findViewById(R.id.back_button);
        back.setOnClickListener(v ->
                {
                    Fragment backFragment = new ProviderProfileFragment();
                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            //TODO: replace R.id with the actual fragment container name
                            .replace(R.id.fragment_container, backFragment)
                            .addToBackStack(null)
                            .commit();
                }
        );

        if (savedInstanceState == null) {
            PasswordRecoveryFragment passwordRecoveryPage = new PasswordRecoveryFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.provider_change_password, passwordRecoveryPage)
                    .commit();
        }
    }
}