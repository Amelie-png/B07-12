package com.example.demoapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class TriageHistoryActivity extends AppCompatActivity {

    private String childId;
    private RecyclerView recyclerView;
    private TriageHistoryAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triage_history);

        db = FirebaseFirestore.getInstance();

        childId = getIntent().getStringExtra("uid");
        if (childId == null) {
            Toast.makeText(this, "Missing child ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        loadHistory();
    }

    private void setupUI() {
        recyclerView = findViewById(R.id.recyclerTriageHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TriageHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        ImageView back = findViewById(R.id.btnBackTriageHistory);
        back.setOnClickListener(v -> finish());
    }

    private void loadHistory() {
        db.collection("triage")
                .whereEqualTo("childId", childId)
                .get()
                .addOnSuccessListener(snap -> {

                    ArrayList<TriageRecord> records = new ArrayList<>();

                    snap.forEach(doc -> {
                        TriageRecord r = new TriageRecord(
                                doc.getString("severity"),
                                doc.getLong("pef") != null ? doc.getLong("pef").intValue() : 0,
                                doc.getLong("rescueAttempts") != null ? doc.getLong("rescueAttempts").intValue() : 0,
                                doc.getBoolean("cantSpeak") != null && doc.getBoolean("cantSpeak"),
                                doc.getBoolean("retractions") != null && doc.getBoolean("retractions"),
                                doc.getBoolean("blueLips") != null && doc.getBoolean("blueLips"),
                                doc.getBoolean("cough") != null && doc.getBoolean("cough"),
                                doc.getBoolean("chestTight") != null && doc.getBoolean("chestTight"),
                                doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0L
                        );
                        records.add(r);
                    });

                    // 🔥 Sort manually (newest → oldest)
                    records.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

                    adapter.updateData(records);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(TriageHistoryActivity.this,
                                "Failed to load history", Toast.LENGTH_SHORT).show()
                );
    }


}
