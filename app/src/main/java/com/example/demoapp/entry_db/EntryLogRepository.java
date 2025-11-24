package com.example.demoapp.entry_db;

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

    public EntryLog parseEntry(String input) {
        String[] sections = input.split("\\|");
        ArrayList<String> symptoms = new ArrayList<>();
        ArrayList<String> triggers = new ArrayList<>();

        String recorder;
        long timestamp;

        int len = sections.length;
        int arrayInput = len - 2;
        for (int i=0; i<arrayInput; i++) {
            String section = sections[i];
            String[] parts = section.split(":");
            if (parts.length == 2) {
                String key = parts[0].trim().toLowerCase();
                String[] values = parts[1].split(",");

                ArrayList<String> valueList = new ArrayList<>();
                for (String value : values) {
                    String trimmed = value.trim();
                    if (!trimmed.isEmpty()) {
                        valueList.add(trimmed);
                    }
                }

                switch (key) {
                    case "symptoms":
                        symptoms.addAll(valueList);
                        break;
                    case "triggers":
                        triggers.addAll(valueList);
                        break;
                }
            }
        }

        recorder = sections[len-2].trim();
        timestamp = Long.parseLong(sections[len-1]);

        return new EntryLog(symptoms, triggers, timestamp, recorder);
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

    public void parseAndSave(String input, OnSuccessListener onSuccess, OnFailureListener onFailure) {
        EntryLog entry = parseEntry(input);
        if (entry != null) {
            saveEntry(entry, onSuccess, onFailure);
        } else {
            onFailure.onFailure(new Exception("Failed to parse input"));
        }
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