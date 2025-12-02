package com.example.demoapp;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * A simple dialog used to display alert messages to a parent user.
 * <p>
 * This dialog is typically shown when the system detects a warning,
 * notification, or emergency-related message triggered by the child’s actions.
 * It presents a message and a single "OK" button that closes the dialog.
 * </p>
 */
public class ParentAlertDialog extends DialogFragment {

    /** The message to be displayed in the alert dialog. */
    private final String alertMessage;

    /**
     * Constructs a ParentAlertDialog containing a specific alert message.
     *
     * @param alertMessage The text content shown to the parent in the dialog.
     */
    public ParentAlertDialog(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    /**
     * Creates and returns the dialog UI for this alert.
     * <p>
     * This method builds an {@link AlertDialog} that contains a title ("Alert"),
     * the message provided to the constructor, and an "OK" button to dismiss it.
     * </p>
     *
     * @param savedInstanceState Restored state for dialog UI (unused)
     * @return A fully configured {@link Dialog} ready to be displayed
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle("Alert")
                .setMessage(alertMessage)
                .setPositiveButton("OK", null)
                .create();
    }
}
