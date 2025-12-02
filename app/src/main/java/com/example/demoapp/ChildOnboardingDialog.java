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
 * A dialog fragment that displays the onboarding information for child users.
 * <p>
 * This dialog is shown the first time a child logs in, explaining how to use the app.
 * After the dialog is closed, the hosting fragment (e.g., {@link ChildHomeFragment})
 * is notified through the {@link OnCloseListener} callback.
 * </p>
 */
public class ChildOnboardingDialog extends DialogFragment {

    /**
     * Listener interface used to notify the host when the dialog is closed.
     * This allows the parent fragment/activity to react to the dismissal event.
     */
    public interface OnCloseListener {
        /**
         * Called when the onboarding dialog is closed by the user.
         */
        void onClose();
    }

    /** Listener instance provided by the hosting Fragment or Activity. */
    private OnCloseListener listener;

    /**
     * Creates a new onboarding dialog with a provided close listener.
     *
     * @param listener Callback invoked when the dialog is dismissed.
     */
    public ChildOnboardingDialog(OnCloseListener listener) {
        this.listener = listener;
    }

    /**
     * Inflates the dialog layout and sets up the close button behavior.
     *
     * @param inflater  The LayoutInflater used to inflate XML layouts.
     * @param container Parent ViewGroup (unused for dialog layout).
     * @param savedInstanceState Previously saved state (unused).
     * @return The inflated onboarding dialog view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_child_onboarding, container, false);

        // Button that closes the dialog
        Button close = view.findViewById(R.id.btnClose);

        close.setOnClickListener(v -> {
            // Notify host if listener exists
            if (listener != null) listener.onClose();

            // Close the dialog
            dismiss();
        });

        return view;
    }
}
