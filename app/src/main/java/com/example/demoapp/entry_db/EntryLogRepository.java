package com.example.demoapp.entry_db;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.util.ArrayList;

public class EntryLogRepository {
    private final CollectionReference entriesCollection = FirebaseFirestore.getInstance().collection("entries");

    public EntryLogRepository() { }

    public void saveEntry(EntryLog entry, OnSuccessListener onSuccess, OnFailureListener onFailure) {
        DocumentReference docRef = entriesCollection.document();
        entry.setId(docRef.getId());

        docRef.set(entry)
                .addOnSuccessListener(aVoid -> {
                    onSuccess.onSuccess(docRef.getId());
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
                        if (entry != null) {
                            entries.add(entry);
                        }
                    }
                    listener.onEntriesRetrieved(entries);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getFilteredEntries(OnEntriesRetrievedListener listener,
                                   String childUid, LocalDate selectedStartDate,
                                   LocalDate selectedEndDate,
                                   ArrayList<String> selectedSymptoms,
                                   ArrayList<String> selectedTriggers){
        entriesCollection.get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<EntryLog> entries = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        EntryLog entry = doc.toObject(EntryLog.class);
                        if(entry != null && childUid.equals(entry.getChildUid())){
                            LocalDate entryDate = LocalDate.parse(entry.getDate());
                            boolean afterStartDate = entryDate.isAfter(selectedStartDate) || entryDate.isEqual(selectedStartDate);
                            boolean beforeEndDate = entryDate.isBefore(selectedEndDate) || entryDate.isEqual(selectedEndDate);

                            ArrayList<CategoryName> entrySymptoms = entry.getSymptoms();
                            ArrayList<CategoryName> entryTriggers = entry.getTriggers();
                            if(entrySymptoms != null && entryTriggers != null && hasMatchingSymptomsTriggers(entrySymptoms, entryTriggers, selectedSymptoms, selectedTriggers)){
                                if (afterStartDate && beforeEndDate) {
                                    entries.add(entry);
                                }
                            }
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

    // Callback interfaces
    public interface OnSuccessListener {
        void onSuccess(String id);
    }

    public interface OnFailureListener {
        void onFailure(Exception e);
    }

    public interface OnEntryRetrievedListener {
        void onEntryRetrieved(EntryLog entry);
        void onError(Exception e);
    }

    public interface OnEntriesRetrievedListener {
        void onEntriesRetrieved(ArrayList<EntryLog> entries);
        void onError(Exception e);
    }
}