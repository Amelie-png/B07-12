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

    public String getEntryNumber() { return entryNumber; }
    public String getTimeRecorded() { return timeRecorded; }
    public String getPerson() { return person; }
    public String getSymptoms() { return symptoms; }
    public String getTriggers() { return triggers; }
}
