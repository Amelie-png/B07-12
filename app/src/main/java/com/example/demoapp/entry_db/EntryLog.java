package com.example.demoapp.entry_db;

import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EntryLog{
    private String childUid;
    private String id;
    private ArrayList<CategoryName> symptoms;
    private ArrayList<CategoryName> triggers;
    private String date;
    //either "Parent" or "Child"
    private String recorder;

    public EntryLog(){
        this.childUid = "";
        this.id = "";
        this.symptoms = new ArrayList<>();
        this.triggers = new ArrayList<>();
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        this.recorder = "";
    }

    public EntryLog(String childId, ArrayList<CategoryName> symptoms, ArrayList<CategoryName> triggers, String date, String recorder){
        this.childUid = childId;
        this.id = "";
        this.symptoms = symptoms;
        this.triggers = triggers;
        this.date = date;
        this.recorder = recorder;
    }

    public String getChildUid() { return childUid; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    //Getters and Setters
    public ArrayList<CategoryName> getSymptoms() { return symptoms; }
    public void setSymptoms(ArrayList<CategoryName> symptoms) { this.symptoms = symptoms; }

    public ArrayList<CategoryName> getTriggers() { return triggers; }
    public void setTriggers(ArrayList<CategoryName> triggers) { this.triggers = triggers; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getRecorder(){ return recorder; }
    public void setRecorder(String r){ recorder = r; }

    @Exclude
    public String getTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date(date));
    }

    @Exclude
    public String getDateTime() {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return dateTimeFormat.format(new Date(date));
    }

}
