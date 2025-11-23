/*package com.example.demoapp.SymptomTriggerLogDB;

import java.util.*;
public class Symptom {
    private String symptomId;
    private String childId;
    private String symptomText;
    private String time;
    private String date;
    private ArrayList<String> triggers;
    private int severity;

    // Empty constructor required for Firestore
    public Symptom() {}

    public Symptom(String childId, String symptomText, String time,
                   String date, List<String> triggers, int severity) {
        this.childId = childId;
        this.symptomText = symptomText;
        this.time = time;
        this.date = date;
        this.triggers = triggers;
        this.severity = severity;
    }

    // Getters and setters for all fields
    public String getSymptomId() { return symptomId; }
    public void setSymptomId(String symptomId) { this.symptomId = symptomId; }

    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public String getSymptomText() { return symptomText; }
    public void setSymptomText(String symptomText) { this.symptomText = symptomText; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<String> getTriggers() { return triggers; }
    public void setTriggers(List<String> triggers) { this.triggers = triggers; }

    public int getSeverity() { return severity; }
    public void setSeverity(int severity) { this.severity = severity; }
}

/*
wheezing

coughing

shortness-of-breath

chest-tightness

night-cough

exercise-trigger

allergen-trigger

cold-air-trigger

rapid-breathing

blue-lips

difficulty-speaking

severe-flare

other
*/