package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
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


        ArrayList<Pair<String, String>> triggers = new ArrayList<>();
        for(String t: TRIGGERS_ARRAY){
            Pair<String, String> p = new Pair<>(t, t);
            triggers.add(p);
        }

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

            ArrayList<Pair<String, String>> selectedPairs = adapter.getSelected();
            ArrayList<Bundle> bundleList = new ArrayList<>();

            for (Pair<String, String> pair : selectedPairs) {
                Bundle b = new Bundle();
                b.putString("category", pair.first);
                b.putString("name", pair.second);
                bundleList.add(b);
            }

            result.putParcelableArrayListExtra("selectedTriggers", bundleList);
            setResult(RESULT_OK, result);
            finish();
        });
    }
}
