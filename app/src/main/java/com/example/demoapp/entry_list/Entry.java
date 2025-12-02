package com.example.demoapp.entry_list;

// Entry.java
public class Entry {
    private String entryNumber;
    private String timeRecorded;
    private String person;
    private String symptoms;
    private String triggers;

    public Entry(String entryNumber, String timeRecorded, String person, String symptoms, String triggers) {
        this.entryNumber = entryNumber;
        this.timeRecorded = timeRecorded;
        this.person = person;
        this.symptoms = symptoms;
        this.triggers = triggers;
    }

    public Entry(String entryNumber, String timeRecorded, String person, String str, boolean isSymptom){
        this.entryNumber = entryNumber;
        this.timeRecorded = timeRecorded;
        this.person = person;
        if(isSymptom){
            this.symptoms = str;
            this.triggers = "<<ACCESS DENIED>>";
        }else{
            this.symptoms = "<<ACCESS DENIED>>";
            this.triggers = str;
        }
    }

    public Entry (String entryNumber, String timeRecorded, String person){
        this.entryNumber = entryNumber;
        this.timeRecorded = timeRecorded;
        this.person = person;
        this.symptoms = "<<ACCESS DENIED>>";
        this.triggers = "<<ACCESS DENIED>>";
    }

    public String getEntryNumber() { return entryNumber; }
    public String getTimeRecorded() { return timeRecorded; }
    public String getPerson() { return person; }
    public String getSymptoms() { return symptoms; }
    public String getTriggers() { return triggers; }
}
