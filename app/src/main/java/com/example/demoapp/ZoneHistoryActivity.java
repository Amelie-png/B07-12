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
    private String childId;

    private List<ZoneHistoryItem> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_history);

        db = FirebaseFirestore.getInstance();
        recycler = findViewById(R.id.recyclerZoneHistory);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        childId = getIntent().getStringExtra("uid"); // use uid

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadZoneHistory();
    }

    private void loadZoneHistory() {

        db.collection("zone")
                .whereEqualTo("childId", childId)   // 单字段查询，不会要求 index
                .get()
                .addOnSuccessListener(query -> {

                    historyList.clear();
                    List<DocumentSnapshot> docs = query.getDocuments();

                    // 🔽 手动按 timestamp 降序排序（不需要 Firestore 排序）
                    docs.sort((a, b) -> {
                        long t1 = a.getLong("timestamp");
                        long t2 = b.getLong("timestamp");
                        return Long.compare(t2, t1); // DESC
                    });

                    // 🔄 转换为 ZoneHistoryItem
                    for (DocumentSnapshot doc : docs) {

                        int percent = doc.getLong("percent").intValue();
                        String zone = doc.getString("zone");
                        long timestamp = doc.getLong("timestamp");

                        historyList.add(new ZoneHistoryItem(percent, zone, timestamp));
                    }

                    recycler.setAdapter(new ZoneHistoryAdapter(historyList));
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

}
