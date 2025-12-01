package com.example.demoapp;

public class ZoneHistoryItem {

    public int percent;
    public String zoneColor;
    public long timestamp;

    public boolean beforeMed;
    public boolean afterMed;

    // Required empty constructor for Firestore
    public ZoneHistoryItem() {}

    // New constructor including medicine flags
    public ZoneHistoryItem(int percent, String zoneColor, long timestamp,
                           boolean beforeMed, boolean afterMed) {
        this.percent = percent;
        this.zoneColor = zoneColor;
        this.timestamp = timestamp;
        this.beforeMed = beforeMed;
        this.afterMed = afterMed;
    }

    public int getPercent() {
        return percent;
    }

    public String getZoneColor() {
        return zoneColor;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isBeforeMed() {
        return beforeMed;
    }

    public boolean isAfterMed() {
        return afterMed;
    }
}
