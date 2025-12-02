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

/**
 * Activity that allows the user (Child, Parent, or Provider)
 * to select asthma triggers from a predefined list.
 *
 * <p>This screen reuses the {@link SymptomsAdapter} to show each
 * trigger with selectable circular icons. Users may also select
 * "Other" to enter a custom trigger name.</p>
 *
 * <p>The selected triggers are returned to the calling Activity
 * via setResult(), where each trigger is packaged inside a Bundle
 * for safe transport and consistency with symptom selection logic.</p>
 */
public class SelectTriggersActivity extends AppCompatActivity {

    /** Adapter responsible for rendering selectable trigger items. */
    private SymptomsAdapter adapter;

    /** Predefined list of common asthma triggers. */
    private static final String[] TRIGGERS_ARRAY = {
            "Dust Mites", "Pets", "Smoke",
            "Strong Odors / Perfumes", "Cold Air",
            "Illness", "Exercise", "Other"
    };

    /**
     * Called when the activity is starting.
     * Sets up UI elements, configures the RecyclerView, and prepares click actions.
     *
     * @param savedInstanceState Saved state bundle from configuration changes.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_triggers);

        // -------------------------------
        // Bind UI components
        // -------------------------------
        TextView buttonBack = findViewById(R.id.buttonBack);
        RecyclerView recyclerView = findViewById(R.id.recyclerTriggers);
        Button buttonAddSelected = findViewById(R.id.buttonAddSelected);

        // -------------------------------------------------------
        // Build ArrayList<Pair<String, String>> from static array
        // (first = category, second = displayed name)
        // -------------------------------------------------------
        ArrayList<Pair<String, String>> triggers = new ArrayList<>();
        for (String t : TRIGGERS_ARRAY) {
            triggers.add(new Pair<>(t, t));
        }

        // -------------------------------------------------------
        // Create adapter: updates button text on selection change
        // -------------------------------------------------------
        adapter = new SymptomsAdapter(
                triggers,
                count -> buttonAddSelected.setText("Add selected (" + count + ")"),
                this
        );

        // Recycler setup
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // -------------------------
        // Back button → close screen
        // -------------------------
        buttonBack.setOnClickListener(v -> finish());

        // --------------------------------------------------------
        // "Add selected" button → return chosen triggers to caller
        // --------------------------------------------------------
        buttonAddSelected.setOnClickListener(v -> {

            Intent result = new Intent();

            ArrayList<Pair<String, String>> selectedPairs = adapter.getSelected();
            ArrayList<Bundle> bundleList = new ArrayList<>();

            // Convert each selected trigger into a Bundle
            for (Pair<String, String> pair : selectedPairs) {
                Bundle b = new Bundle();
                b.putString("category", pair.first);
                b.putString("name", pair.second);
                bundleList.add(b);
            }

            // Return list of Bundles to calling activity
            result.putParcelableArrayListExtra("selectedTriggers", bundleList);
            setResult(RESULT_OK, result);
            finish();
        });
    }
}
