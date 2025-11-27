package com.example.demoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ChildOnboardingDialog extends DialogFragment {

    public interface OnCloseListener {
        void onClose();
    }

    private OnCloseListener listener;

    public ChildOnboardingDialog(OnCloseListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_child_onboarding, container, false);

        Button close = view.findViewById(R.id.btnClose);
        close.setOnClickListener(v -> {
            if (listener != null) listener.onClose();
            dismiss();
        });

        return view;
    }
}

