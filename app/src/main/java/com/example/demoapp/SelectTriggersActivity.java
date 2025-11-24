package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.symptoms.SymptomsAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class SelectTriggersActivity extends AppCompatActivity {

    private SymptomsAdapter adapter;

    private static final String[] TRIGGERS_ARRAY = {
            "Dust Mites", "Pets", "Smoke",
            "Strong Odors / Perfumes", "Cold Air",
            "Illness", "Exercise", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_triggers);

        TextView buttonBack = findViewById(R.id.buttonBack);
        RecyclerView recyclerView = findViewById(R.id.recyclerTriggers);
        Button buttonAddSelected = findViewById(R.id.buttonAddSelected);

        ArrayList<String> triggers = new ArrayList<>(Arrays.asList(TRIGGERS_ARRAY));

        // ⭐ Correct 3-argument constructor
        adapter = new SymptomsAdapter(
                triggers,
                count -> buttonAddSelected.setText("Add selected (" + count + ")"),
                this   // ← Context REQUIRED
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> finish());

        buttonAddSelected.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putStringArrayListExtra("selectedTriggers", adapter.getSelected());
            setResult(RESULT_OK, result);
            finish();
        });
    }
}
