package com.example.demoapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HomeStepsCardActivity extends AppCompatActivity {

    private static final String EXTRA_ZONE = "zone";

    private CountDownTimer timer;
    private long timeLeft = 10 * 60 * 1000; // 10 minutes

    private String zone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_home_steps);

        // Read "zone" argument
        Intent intent = getIntent();
        zone = intent.getStringExtra(EXTRA_ZONE);

        TextView timerTxt = findViewById(R.id.textTimer);
        Button backHomeBtn = findViewById(R.id.btnBackHome);

        // TODO: Show different steps depending on zone
        // e.g. if "YELLOW", show yellow steps

        startTimer(timerTxt);

        backHomeBtn.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            finish(); // close activity, go back home
        });
    }

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

    private void showRecheckPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Re-check")
                .setMessage("Are symptoms improving?")
                .setPositiveButton("Yes", (d, w) -> finish())
                .setNegativeButton("Worse", (d, w) -> escalateToEmergency())
                .setCancelable(false)
                .show();
    }

    private void escalateToEmergency() {
        Intent intent = new Intent(this, EmergencyCardActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
