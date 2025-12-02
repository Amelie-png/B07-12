package com.example.demoapp.provider_report;

import com.example.demoapp.entry_db.CategoryName;
import com.example.demoapp.entry_db.EntryLog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.List;

public class CountProblemDays {
    private int countProblemDays(List<EntryLog> entries) {
        int count = 0;

        for (EntryLog e : entries) {
            List<CategoryName> symptoms = e.getSymptoms();
            if (symptoms != null && !symptoms.isEmpty()) {
                count++;
            }
        }

        return count;
    }
}
