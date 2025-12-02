package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ViewDailyEntryActivity extends AppCompatActivity {

    Button backButton;
    String Uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_daily_entry);

        Uid = getIntent().getExtras().getString("Uid");

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewDailyEntryActivity.this, MainNavActivity.class);
            intent.putExtra("Uid", getIntent().getStringExtra("Uid"));
            startActivity(intent);
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
            bundle.putString("childUid", getIntent().getStringExtra("childUid"));
            bundle.putString("uid", getIntent().getStringExtra("uid"));
            bundle.putString("role", getIntent().getStringExtra("role"));

            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.daily_entry_container, fragment)
                    .commit();
        }
    }
}
