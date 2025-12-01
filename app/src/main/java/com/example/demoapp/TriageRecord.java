
package com.example.demoapp;

public class TriageRecord {

    public String severity;
    public int pef;
    public int rescueAttempts;

    public boolean cantSpeak, retractions, blueLips;
    public boolean cough, chestTight;

    public long timestamp;

    public TriageRecord(String severity, int pef, int rescueAttempts,
                        boolean cantSpeak, boolean retractions, boolean blueLips,
                        boolean cough, boolean chestTight,
                        long timestamp) {

        this.severity = severity;
        this.pef = pef;
        this.rescueAttempts = rescueAttempts;
        this.cantSpeak = cantSpeak;
        this.retractions = retractions;
        this.blueLips = blueLips;
        this.cough = cough;
        this.chestTight = chestTight;
        this.timestamp = timestamp;
    }
}
