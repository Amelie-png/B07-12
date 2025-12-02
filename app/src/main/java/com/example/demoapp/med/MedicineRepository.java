package com.example.demoapp.med;

import android.util.Log;

import com.example.demoapp.Badge;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
    public void saveControllerMed(String childId, ControllerMed med, OnResult<Void> cb) {
        childRef.document(childId)
                .update("controller", med.toMap())
                .addOnSuccessListener(a -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    // Save/update rescue med settings given childId
    public void saveRescueMed(String childId, RescueMed med, OnResult<Void> cb) {
        childRef.document(childId)
                .update("rescue", med.toMap())
                .addOnSuccessListener(a -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    // Fetch controller med map
    public void loadControllerMed(String childId, OnResult<ControllerMed> cb) {
        childRef.document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    Map<String, Object> map = (Map<String, Object>) doc.get("controller");
                    ControllerMed med = ControllerMed.fromMap(map);
                    cb.onSuccess(med);
                })
                .addOnFailureListener(cb::onFailure);
    }

    // Fetch rescue med map
    public void loadRescueMed(String childId, OnResult<RescueMed> cb) {
        childRef.document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    Map<String, Object> map = (Map<String, Object>) doc.get("rescue");
                    RescueMed med = RescueMed.fromMap(map);
                    cb.onSuccess(med);
                })
                .addOnFailureListener(cb::onFailure);
    }

    // Fetch share code info
    public void fetchShareCode(String childId, String providerId, OnResult<Map<String, Object>> cb){
        childRef.document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    Map<String,Object> shareCodes = (Map<String,Object>) doc.get("shareCodes");
                    if(shareCodes == null) {
                        Log.e("DB", "No shareCodes field found");
                        return;
                    }

                    Map<String, Object> matchedShareCode = null;

                    for (Map.Entry<String, Object> entry : shareCodes.entrySet()) {
                        Map<String, Object> value = (Map<String, Object>) entry.getValue();

                        String id = (String) value.get("providerId");

                        if (id != null && id.equals(providerId)) {
                            matchedShareCode = value;
                            break;
                        }
                    }

                    if (matchedShareCode != null) {
                        Log.d("DB", "Found share code: " + matchedShareCode);
                        cb.onSuccess(matchedShareCode);
                    } else {
                        cb.onFailure(new Exception("ShareCode not found for providerId: " + providerId));
                    }
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

    public void updateStreaksOnNewLog(String childId, boolean techniqueCompleted) {
        DocumentReference childDoc = childRef.document(childId);
        long today = System.currentTimeMillis();

        childDoc.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) return;

            // ---------- LOAD CURRENT BADGES ----------
            Map<String, Object> badges = (Map<String, Object>) snapshot.get("badges");
            if (badges == null) badges = new HashMap<>();

            // Create a typed Badge object for easier handling
            Badge badge = Badge.fromMap(badges);

            // ---------- UPDATE CONTROLLER STREAK ----------
            badge.setControllerStreak(
                    MedicineUtils.isSameDay(badge.getLastCheckedDate(), today) ? badge.getControllerStreak() :
                            MedicineUtils.isYesterday(badge.getLastCheckedDate(), today) ? badge.getControllerStreak() + 1 :
                                    1);

            // ---------- UPDATE TECHNIQUE STREAK ----------
            if (techniqueCompleted) {
                badge.setControllerStreak(
                        MedicineUtils.isSameDay(badge.getLastTechniqueDate(), today) ? badge.getTechniqueStreak() :
                                MedicineUtils.isYesterday(badge.getLastTechniqueDate(), today) ? badge.getTechniqueStreak() + 1 :
                                        1);

                // update technique date only if used
                badge.setLastTechniqueDate(today);
            }

            // Always update last checked date for controller streak
            badge.setLastCheckedDate(today);

            // ---------- SAVE BACK TO FIRESTORE ----------
            childDoc.update("badges", Badge.toMap(badge));
        });
    }


//    public void updateControllerStreakOnNewLog(String childId) {
//
//        DocumentReference childDoc = childRef.document(childId);
//
//        long today = System.currentTimeMillis();
//
//        childDoc.get().addOnSuccessListener(snapshot -> {
//            if (!snapshot.exists()) return;
//
//            Map<String, Object> badges = (Map<String, Object>) snapshot.get("badges");
//            if (badges == null) badges = new HashMap<>();
//
//            Long lastLogDate = badges.get("lastCheckedDate") instanceof Long ?
//                    (Long) badges.get("lastCheckedDate") : null;
//
//            Long controllerStreak = badges.get("controllerStreak") instanceof Long ?
//                    (Long) badges.get("controllerStreak") : 0L;
//
//            boolean sameDay = lastLogDate != null && MedicineUtils.isSameDay(lastLogDate, today);
//            boolean yesterday = lastLogDate != null && MedicineUtils.isYesterday(lastLogDate, today);
//
//            long newStreak;
//
//            if (sameDay) {
//                newStreak = controllerStreak;
//            } else if (yesterday) {
//                newStreak = controllerStreak + 1;
//            } else {
//                newStreak = 1;
//            }
//
//            badges.put("controllerStreak", newStreak);
//            badges.put("lastCheckedDate", today);
//
//            childDoc.update("badges", badges);
//        });
//    }
//
//    public void updateTechniqueStreakOnNewLog(String childId) {
//
//        DocumentReference childDoc = childRef.document(childId);
//
//        long today = System.currentTimeMillis();
//
//        childDoc.get().addOnSuccessListener(snapshot -> {
//            if (!snapshot.exists()) return;
//
//            Map<String, Object> badges = (Map<String, Object>) snapshot.get("badges");
//            if (badges == null) badges = new HashMap<>();
//
//            Long lastLogDate = badges.get("lastCheckedDate") instanceof Long ?
//                    (Long) badges.get("lastCheckedDate") : null;
//
//            Long techniqueStreak = badges.get("techniqueStreak") instanceof Long ?
//                    (Long) badges.get("techniqueStreak") : 0L;
//
//            boolean sameDay = lastLogDate != null && MedicineUtils.isSameDay(lastLogDate, today);
//            boolean yesterday = lastLogDate != null && MedicineUtils.isYesterday(lastLogDate, today);
//
//            long newStreak;
//
//            if (sameDay) {
//                newStreak = techniqueStreak;
//            } else if (yesterday) {
//                newStreak = techniqueStreak + 1;
//            } else {
//                newStreak = 1;
//            }
//
//            badges.put("techniqueStreak", newStreak);
//            badges.put("lastCheckedDate", today);
//
//            childDoc.update("badges", badges);
//        });
//    }

}
