package com.example.demoapp.entry_db;

import android.widget.Toast;

import com.google.firebase.firestore.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EntryLogRepository {
    private final CollectionReference entriesCollection;

    public EntryLogRepository() {
        entriesCollection = FirebaseFirestore.getInstance().collection("entries");
    }

    public void saveEntry(EntryLog entry, android.content.Context context, OnSuccessListener onSuccess, OnFailureListener onFailure) {
        entriesCollection
                .whereEqualTo("childUid", entry.getChildUid())
                .whereEqualTo("date", entry.getDate())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        DocumentReference docRef = entriesCollection.document();
                        entry.setId(docRef.getId());

                        docRef.set(entry)
                                .addOnSuccessListener(aVoid -> onSuccess.onSuccess(docRef.getId()))
                                .addOnFailureListener(onFailure::onFailure);
                        Toast.makeText(context, "Saved Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "A Daily Check-in for today already exists!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(onFailure::onFailure);
    }

    public void getEntry(String id, OnEntryRetrievedListener listener) {
        entriesCollection.document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    EntryLog entry = documentSnapshot.toObject(EntryLog.class);
                    listener.onEntryRetrieved(entry);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getAllEntries(OnEntriesRetrievedListener listener) {
        entriesCollection.orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<EntryLog> entries = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        EntryLog entry = doc.toObject(EntryLog.class);
                        if (entry != null) entries.add(entry);
                    }
                    listener.onEntriesRetrieved(entries);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getFilteredEntries(OnEntriesRetrievedListener listener,
                                   String childUid, String selectedStartDate,
                                   String selectedEndDate,
                                   ArrayList<String> selectedSymptoms,
                                   ArrayList<String> selectedTriggers){
        entriesCollection
                .whereEqualTo("childUid", childUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<EntryLog> entries = new ArrayList<>();
                    List<DocumentSnapshot> docs =  querySnapshot.getDocuments();
                    for (DocumentSnapshot doc : docs) {
                        EntryLog entry = doc.toObject(EntryLog.class);
                        String entryDateString = entry.getDate();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate entryDate = LocalDate.parse(entryDateString, formatter);
                        LocalDate startDate = LocalDate.parse(selectedStartDate, formatter);
                        LocalDate endDate = LocalDate.parse(selectedEndDate, formatter);

                        boolean afterStartDate = entryDate.isAfter(startDate) || entryDate.isEqual(startDate);
                        boolean beforeEndDate = entryDate.isBefore(endDate) || entryDate.isEqual(endDate);
                        if(entry != null && hasMatchingSymptomsTriggers(
                                entry.getSymptoms(), entry.getTriggers(),
                                selectedSymptoms, selectedTriggers)
                                && afterStartDate
                                && beforeEndDate) {
                            entries.add(entry);
                        }
                    }
                    listener.onEntriesRetrieved(entries);
                })
                .addOnFailureListener(listener::onError);
    }

    private boolean hasMatchingSymptomsTriggers(ArrayList<CategoryName> entrySymptoms,
                                                ArrayList<CategoryName> entryTriggers,
                                                ArrayList<String> selectedSymptoms,
                                                ArrayList<String> selectedTriggers) {
        return containEntryStrings(entrySymptoms, selectedSymptoms) && containEntryStrings(entryTriggers, selectedTriggers);
    }

    private boolean containEntryStrings(ArrayList<CategoryName> entryValues,
                                        ArrayList<String> selectedValues){
        for(String selectedSymptom : selectedValues){
            for(int j = 0; j < entryValues.size(); j++){
                CategoryName p = entryValues.get(j);
                if(p.getCategory().equals(selectedSymptom)){
                    break;
                }
                if(j == entryValues.size()-1){
                    return false;
                }
            }
        }
        return true;
    }

    public void countProblemDaysByCategories(String childUid, LocalDate startDate, LocalDate endDate,
                                             ArrayList<String> categories,
                                             OnProblemDaysCountedByCategoryListener listener) {
        entriesCollection
                .whereEqualTo("childUid", childUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Integer> counts = new HashMap<>();
                    for (String cat : categories) counts.put(cat, 0);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                    for (DocumentSnapshot doc : docs) {
                        EntryLog entry = doc.toObject(EntryLog.class);
                        if (entry == null) continue;

                        LocalDate entryDate = LocalDate.parse(entry.getDate(), formatter);
                        if (entryDate.isBefore(startDate) || entryDate.isAfter(endDate)) continue;

                        List<CategoryName> symptoms = entry.getSymptoms();
                        if (symptoms != null) {
                            for (CategoryName symptom : symptoms) {
                                String catName = symptom.getCategory();
                                if (counts.containsKey(catName)) {
                                    counts.put(catName, counts.get(catName) + 1);
                                }
                            }
                        }
                    }

                    listener.onCounted(counts);
                })
                .addOnFailureListener(listener::onError);
    }

    public interface OnProblemDaysCountedByCategoryListener {
        void onCounted(Map<String, Integer> counts);
        void onError(Exception e);
    }


    public interface OnSuccessListener { void onSuccess(String id); }
    public interface OnFailureListener { void onFailure(Exception e); }
    public interface OnEntryRetrievedListener { void onEntryRetrieved(EntryLog entry); void onError(Exception e); }
    public interface OnEntriesRetrievedListener { void onEntriesRetrieved(ArrayList<EntryLog> entries); void onError(Exception e); }
}
