package com.example.demoapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that displays step-by-step instructions for managing symptoms at home,
 * depending on the child's zone (GREEN, YELLOW, etc.). This screen includes:
 *
 * <ul>
 *     <li>A 10-minute countdown timer</li>
 *     <li>A step list (currently TODO)</li>
 *     <li>A button to return home</li>
 *     <li>A popup when time runs out asking if symptoms improved</li>
 * </ul>
 *
 * <p>If symptoms worsen, the activity routes the user to
 * {@link EmergencyCardActivity}.</p>
 */
public class HomeStepsCardActivity extends AppCompatActivity {

    /** Key used for retrieving the zone (e.g., "YELLOW") passed into this Activity. */
    private static final String EXTRA_ZONE = "zone";

    /** Timer responsible for the 10-minute countdown. */
    private CountDownTimer timer;

    /** Initial countdown duration: 10 minutes in milliseconds. */
    private long timeLeft = 10 * 60 * 1000;

    /** Zone value passed into this activity (“GREEN”, “YELLOW”, etc.). */
    private String zone;

    /**
     * Activity lifecycle callback.
     *
     * <p>This method:
     * <ul>
     *     <li>Inflates the layout</li>
     *     <li>Retrieves the symptom zone argument</li>
     *     <li>Starts the countdown timer</li>
     *     <li>Handles the Back to Home button</li>
     * </ul>
     *
     * @param savedInstanceState Previously saved state (unused)
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_home_steps);

        // Retrieve the zone argument passed from previous screen
        Intent intent = getIntent();
        zone = intent.getStringExtra(EXTRA_ZONE);

        TextView timerTxt = findViewById(R.id.textTimer);
        Button backHomeBtn = findViewById(R.id.btnBackHome);

        // TODO: Display different instructions based on zone (e.g., Yellow steps vs. Green steps)

        // Begin countdown timer
        startTimer(timerTxt);

        // When pressed → Stop timer & return home
        backHomeBtn.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            finish();
        });
    }

    /**
     * Starts a countdown timer and updates the UI every second.
     *
     * @param timerTxt TextView displaying the remaining time in MM:SS format
     */
    private void startTimer(TextView timerTxt) {
        timer = new CountDownTimer(timeLeft, 1000) {

            @Override
            public void onTick(long ms) {
                timeLeft = ms;

                int minutes = (int) (ms / 1000) / 60;
                int seconds = (int) (ms / 1000) % 60;

                timerTxt.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerTxt.setText("00:00");
                showRecheckPopup();
            }
        };

        timer.start();
    }

    /**
     * Displays a dialog asking the user if symptoms have improved after
     * the timer ends.
     *
     * <p>If symptoms improved → close the card.<br>
     * If symptoms worsened → escalate to emergency instructions.</p>
     */
    private void showRecheckPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Re-check")
                .setMessage("Are symptoms improving?")
                .setPositiveButton("Yes", (d, w) -> finish())
                .setNegativeButton("Worse", (d, w) -> escalateToEmergency())
                .setCancelable(false)
                .show();
    }

    /**
     * Navigates the user to {@link EmergencyCardActivity} when symptoms worsen.
     */
    private void escalateToEmergency() {
        Intent intent = new Intent(this, EmergencyCardActivity.class);
        startActivity(intent);
    }

    /**
     * Ensures that the countdown timer is stopped when the Activity is destroyed
     * to prevent memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
