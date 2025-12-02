package com.example.demoapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity displaying safety or health advice for the "Green Zone".
 *
 * <p>This screen is part of the triage workflow. It provides non-emergency
 * recommendations and guidance to the user when symptoms and readings fall
 * into the safest range.</p>
 *
 * <p>The screen consists of a single "OK" button that closes the activity
 * and returns the user to the previous screen (usually TriageActivity
 * or ChildHomeFragment).</p>
 */
public class GreenAdviceCardActivity extends AppCompatActivity {

    /**
     * Standard Activity lifecycle callback.
     *
     * <p>This method:
     * <ul>
     *     <li>Initializes the view layout</li>
     *     <li>Binds the "OK" button</li>
     *     <li>Closes the screen when the button is clicked</li>
     * </ul>
     *
     * @param savedInstanceState Previously saved Activity state (unused here)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_green_advice);

        // Bind OK button
        Button ok = findViewById(R.id.btnOkGreen);

        // Close this screen and return to previous
        ok.setOnClickListener(v -> finish());
    }
}
