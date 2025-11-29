package com.example.demoapp;

public class ZoneHistoryItem {
    public int percent;
    public String zoneColor;
    public long timestamp;

    // Required empty constructor for Firestore deserialization
    public ZoneHistoryItem() {}

    public ZoneHistoryItem(int percent, String zoneColor, long timestamp) {
        this.percent = percent;
        this.zoneColor = zoneColor;
        this.timestamp = timestamp;
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
}
