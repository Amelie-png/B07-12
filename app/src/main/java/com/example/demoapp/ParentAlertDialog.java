package com.example.demoapp;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ParentAlertDialog extends DialogFragment {

    private final String alertMessage;

    public ParentAlertDialog(String alertMessage) {
        this.alertMessage = alertMessage;
    }

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
