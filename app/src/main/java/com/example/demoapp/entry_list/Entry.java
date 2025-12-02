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

    public Entry() { } // 默认无参构造

    public String getEntryNumber() { return entryNumber; }
    public void setEntryNumber(String entryNumber) { this.entryNumber = entryNumber; }

    public String getTimeRecorded() { return timeRecorded; }
    public void setTimeRecorded(String timeRecorded) { this.timeRecorded = timeRecorded; }

    public String getPerson() { return person; }
    public void setPerson(String person) { this.person = person; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getTriggers() { return triggers; }
    public void setTriggers(String triggers) { this.triggers = triggers; }
}
