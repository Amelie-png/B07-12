package com.example.demoapp;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class AddPatientPopup extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate popup layout
        View view = inflater.inflate(R.layout.pop_up_add_patient, container, false);

        // Close button inside pop-up
        Button closeButton = view.findViewById(R.id.add_patient_close_button);
        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            // Make dialogue background fully transparent
            getDialog().getWindow().setBackgroundDrawable(
                    new InsetDrawable(new ColorDrawable(Color.TRANSPARENT), 0)
            );
        }
    }
}
