package com.example.demoapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ZoneHistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recycler;
    private String childId;

    private List<ZoneHistoryItem> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_history);

        db = FirebaseFirestore.getInstance();
        recycler = findViewById(R.id.recyclerZoneHistory);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        childId = getIntent().getStringExtra("uid");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadZoneHistory();
    }

    private void loadZoneHistory() {

        db.collection("zone")
                .whereEqualTo("childId", childId)
                .get()
                .addOnSuccessListener(query -> {

                    historyList.clear();
                    List<DocumentSnapshot> docs = query.getDocuments();

                    docs.sort((a, b) -> {
                        long t1 = a.getLong("timestamp");
                        long t2 = b.getLong("timestamp");
                        return Long.compare(t2, t1); // DESC
                    });

                    for (DocumentSnapshot doc : docs) {

                        int percent = doc.getLong("percent").intValue();
                        String zone = doc.getString("zone");
                        long timestamp = doc.getLong("timestamp");

                        boolean beforeMed = doc.contains("beforeMed") && Boolean.TRUE.equals(doc.getBoolean("beforeMed"));
                        boolean afterMed  = doc.contains("afterMed")  && Boolean.TRUE.equals(doc.getBoolean("afterMed"));

                        historyList.add(new ZoneHistoryItem(
                                percent,
                                zone,
                                timestamp,
                                beforeMed,
                                afterMed
                        ));
                    }

                    recycler.setAdapter(new ZoneHistoryAdapter(historyList));
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

}
