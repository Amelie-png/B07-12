package com.example.demoapp.entry_db;

import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;import com.example.demoapp.med.MedicineEntry;

public class EntryLog{
    private String childUid;
    private String id;
    public List<MedicineEntry> medicineEntries; // 用 public 或者 getter
    public String triageState;
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

    public List<MedicineEntry> getMedicineEntries() { return medicineEntries; }
    public String getTriageState() { return triageState; }

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
