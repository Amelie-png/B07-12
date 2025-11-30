package com.example.demoapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GreenAdviceCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_green_advice);

        Button ok = findViewById(R.id.btnOkGreen);

        ok.setOnClickListener(v -> {
            // Simply close this screen and return to Triage or Home
            finish();
        });
    }
}
