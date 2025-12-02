package com.example.demoapp.med;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.demoapp.models.Child;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private final CollectionReference childRef;

    public MedicineRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        entryRef = db.collection("medEntries");
        childRef = db.collection("children");
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

    // Save/update controller med settings given childId
    public void saveControllerMed(String childId, ControllerMed controller, OnResult<Void> cb) {
        Map<String, Object> controllerMap = controller.toMap();
        childRef.document(childId).update("controller", controllerMap)
                .addOnSuccessListener(a -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    // Save/update rescue med settings given childId
    public void saveRescueMed(String childId, RescueMed rescue, OnResult<Void> cb) {
        Map<String, Object> rescueMap = rescue.toMap();
        childRef.document(childId).update("rescue", rescueMap)
                .addOnSuccessListener(a -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    // Fetch controller med map
    public void loadControllerMed(String childId, OnResult<Map<String, Object>> cb) {
        childRef.document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    Map<String,Object> map = (Map<String,Object>) doc.get("controller");
                    cb.onSuccess(map);
                })
                .addOnFailureListener(cb::onFailure);
    }

    // Fetch rescue med map
    public void loadRescueMed(String childId, OnResult<Map<String, Object>> cb) {
        childRef.document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    Map<String,Object> map = (Map<String,Object>) doc.get("rescue");
                    cb.onSuccess(map);
                })
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
        }

        // Timestamp filters given date range (optional)
        q = q.whereGreaterThanOrEqualTo("timestamp", dateFromEpoch)
                .whereLessThanOrEqualTo("timestamp", dateToEpoch);

        // Get all filtered data in descending order (ie latest first)
        q.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
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
                    cb.onSuccess(list);
                });
    }
}
