package com.example.demoapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ViewDailyEntryActivity extends AppCompatActivity {

    Button backButton;
    String uid;
    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_daily_entry);

        uid = getIntent().getExtras().getString("uid");

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            finish();
        });

        if (savedInstanceState == null) {
            DailyEntryDisplayScreen fragment = new DailyEntryDisplayScreen();

            // Pass extras from intent
            Bundle bundle = new Bundle();
            bundle.putString("startDate", getIntent().getStringExtra("startDate"));
            bundle.putString("endDate", getIntent().getStringExtra("endDate"));
            bundle.putStringArrayList("symptoms", getIntent().getStringArrayListExtra("symptoms"));
            bundle.putStringArrayList("triggers", getIntent().getStringArrayListExtra("triggers"));
            bundle.putString("childId", getIntent().getStringExtra("childUid"));
            bundle.putString("uid", getIntent().getStringExtra("uid"));
            bundle.putString("role", getIntent().getStringExtra("role"));
            bundle.putBoolean("symptomsAllowed", getIntent().getBooleanExtra("symptomsAllowed", true));
            bundle.putBoolean("triggersAllowed", getIntent().getBooleanExtra("triggersAllowed",true));

            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.daily_entry_container, fragment)
                    .commit();
        }
    }
}
