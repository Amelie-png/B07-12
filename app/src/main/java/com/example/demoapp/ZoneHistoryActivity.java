package com.example.demoapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ZoneHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recycler;
    private List<ZoneHistoryItem> historyList = new ArrayList<>();
    private String childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_history);

        db = FirebaseFirestore.getInstance();
        recycler = findViewById(R.id.recyclerZoneHistory);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Get childId from intent
        childId = getIntent().getStringExtra("uid");

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadZoneHistory();
    }

    private void loadZoneHistory() {
        db.collection("zone")
                .whereEqualTo("childId", childId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {

                    historyList.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        int percent = doc.getLong("percent").intValue();
                        String color = doc.getString("zone");
                        long timestamp = doc.getLong("timestamp");

                        historyList.add(new ZoneHistoryItem(percent, color, timestamp));
                    }

                    recycler.setAdapter(new ZoneHistoryAdapter(historyList));
                });
    }
}
