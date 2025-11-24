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

public class SelectSymptomsActivity extends AppCompatActivity {

    private SymptomsAdapter adapter;

    private static final String[] SYMPTOMS_ARRAY = {
            "Cough", "Wheezing", "Shortness of Breath",
            "Chest Tightness", "Difficulty Breathing",
            "Rapid Breathing", "Fatigue", "Lightheadedness",
            "Throat Irritation", "Chest Discomfort",
            "Nasal Congestion", "Runny Nose", "Sneezing",
            "Itchy Eyes", "Sore Throat", "Headache",
            "Trouble Sleeping", "Reduced Exercise Tolerance",
            "Increased Mucus", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_symptoms);

        TextView buttonBack = findViewById(R.id.buttonBack);
        RecyclerView recyclerView = findViewById(R.id.recyclerSymptoms);
        Button buttonAddSelected = findViewById(R.id.buttonAddSelected);

        ArrayList<String> symptoms = new ArrayList<>(Arrays.asList(SYMPTOMS_ARRAY));

        // ⭐ UPDATED: Added "this" as context in the constructor
        adapter = new SymptomsAdapter(
                symptoms,
                count -> buttonAddSelected.setText("Add selected (" + count + ")"),
                this   // <-- REQUIRED so "Other" dialog works
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> finish());

        buttonAddSelected.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putStringArrayListExtra("selectedSymptoms", adapter.getSelected());
            setResult(RESULT_OK, result);
            finish();
        });
    }
}
