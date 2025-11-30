package com.example.demoapp.med;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicineRepository {
    private final CollectionReference entryRef;

    public MedicineRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        entryRef = db.collection("medEntries");
    }

    public interface OnResult<T> {
        void onSuccess(T result);

        void onFailure(Exception e);
    }

    // Add a med log. This uses server timestamp for takenAt
    public void addMedLog(MedicineEntry entry, OnResult<String> cb) {
        Map<String, Object> map = entry.toMap();
        entryRef.add(map).addOnSuccessListener(doc -> cb.onSuccess(doc.getId()))
                .addOnFailureListener(cb::onFailure);
    }

    // Update doseCount only (used by edit dose dialog)
    public void updateDoseCount(String entryId, int newDoseCount, OnResult<Void> cb) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("doseCount", newDoseCount);
        entryRef.document(entryId).update(updates).addOnSuccessListener(a -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    // Fetch logs (query with optional filters)
    public void fetchLogs(String childId, String medicineTypeFilter, long dateFromEpoch, long dateToEpoch, OnResult<List<MedicineEntry>> cb) {
        Query q = entryRef;
        Log.d("DEBUG_FETCH", "Fetching logs for childId=" + childId);

        // Filter by child (mandatory)
        if (childId != null && !childId.isEmpty()) {
            q = q.whereEqualTo("childId", childId);
            Log.d("DEBUG_FETCH", "Applied filter: childId");
        }

        // Type (rescue vs controller) filter (optional)
        if (medicineTypeFilter != null) {
            q = q.whereEqualTo("medType", medicineTypeFilter);
            Log.d("DEBUG_FETCH", "Applied filter: medType=" + medicineTypeFilter);
        }

        // Timestamp filters given date range (optional)
        q = q.whereGreaterThanOrEqualTo("timestamp", dateFromEpoch)
                .whereLessThanOrEqualTo("timestamp", dateToEpoch);
        Log.d("DEBUG_FETCH", "Final query ready. Executing Firestore request.");

        // Get all filtered data in descending order (ie latest first)
        q.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        Log.e("DEBUG_FETCH", "Fetch FAILED", task.getException());
                        cb.onFailure(task.getException());
                        return;
                    }
                    List<MedicineEntry> list = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        MedicineEntry entry = doc.toObject(MedicineEntry.class);
                        if (entry != null) {
                            entry.setEntryId(doc.getId());
                            // set timestamp
                            Long time = doc.getLong("timestamp");
                            if (time != null) entry.setTimestamp(time);
                            list.add(entry);
                        }
                    }
                    Log.d("DEBUG_FETCH", "Fetched logs count=" + list.size());
                    cb.onSuccess(list);
                });
    }
}
