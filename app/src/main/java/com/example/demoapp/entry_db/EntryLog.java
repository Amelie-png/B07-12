package com.example.demoapp.entry_db;

import android.util.Pair;

import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EntryLog{
    private String id;
    private ArrayList<Pair<String, String>> symptoms;
    private ArrayList<Pair<String, String>> triggers;
    private long timestamp;
    //either "Parent" or "Child"
    private String recorder;

    public EntryLog(){
        this.id = "";
        this.symptoms = new ArrayList<>();
        this.triggers = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
        this.recorder = "";
    }

    public EntryLog(ArrayList<Pair<String, String>> symptoms, ArrayList<Pair<String, String>> triggers, long timestamp, String recorder){
        this.id = "";
        this.symptoms = symptoms;
        this.triggers = triggers;
        this.timestamp = timestamp;
        this.recorder = recorder;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    //Getters and Setters
    public ArrayList<Pair<String, String>> getSymptoms() { return symptoms; }
    public void setSymptoms(ArrayList<Pair<String, String>> symptoms) { this.symptoms = symptoms; }

    public ArrayList<Pair<String, String>> getTriggers() { return triggers; }
    public void setTriggers(ArrayList<Pair<String, String>> triggers) { this.triggers = triggers; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getRecorder(){ return recorder; }
    public void setRecorder(String r){ recorder = r; }

    @Exclude
    public String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    @Exclude
    public String getTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date(timestamp));
    }

    @Exclude
    public String getDateTime() {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return dateTimeFormat.format(new Date(timestamp));
    }

}
