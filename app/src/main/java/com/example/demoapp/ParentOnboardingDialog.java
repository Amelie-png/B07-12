package com.example.demoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog displayed to parents the first time they log into the application.
 *
 * <p>This dialog is part of the onboarding workflow and provides guidance
 * or instructions for new parent users. It includes a single dismiss button
 * to close the dialog.</p>
 */
public class ParentOnboardingDialog extends DialogFragment {

    /**
     * Required empty public constructor.
     *
     * <p>DialogFragments must have an empty constructor so they can be
     * recreated by Android when needed (e.g., after configuration
     * changes or process recreation).</p>
     */
    public ParentOnboardingDialog() {
        // Required empty constructor
    }

    /**
     * Inflates the dialog UI and sets up the close button.
     *
     * @param inflater  LayoutInflater used to inflate the XML layout
     * @param container The parent container the dialog attaches to
     * @param savedInstanceState Saved UI state for dialog recreation (unused)
     * @return The inflated dialog view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate dialog layout
        View view = inflater.inflate(R.layout.dialog_parent_onboarding, container, false);

        // Bind "close" button
        Button close = view.findViewById(R.id.closeParentPopup);

        // Close the dialog when tapped
        close.setOnClickListener(v -> dismiss());

        return view;
    }
}
