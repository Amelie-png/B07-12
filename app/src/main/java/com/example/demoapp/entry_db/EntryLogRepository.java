package com.example.demoapp.entry_db;

import android.util.Pair;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.util.ArrayList;

public class EntryLogRepository {
    private final CollectionReference entriesCollection;

    public EntryLogRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.entriesCollection = db.collection("entries");
    }

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

    public void getSelectedDateEntries(OnEntriesRetrievedListener listener, LocalDate selectedStartDate, LocalDate selectedEndDate){
        entriesCollection.orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<EntryLog> entries = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        EntryLog entry = doc.toObject(EntryLog.class);
                        LocalDate entryDate = LocalDate.parse(entry.getDate());
                        boolean afterStartDate = entryDate.isAfter(selectedStartDate) || entryDate.isEqual(selectedStartDate);
                        boolean beforeEndDate = entryDate.isBefore(selectedEndDate) || entryDate.isEqual(selectedEndDate);
                        if (afterStartDate && beforeEndDate) {
                            entries.add(entry);
                        }
                    }
                    listener.onEntriesRetrieved(entries);
                })
                .addOnFailureListener(listener::onError);
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