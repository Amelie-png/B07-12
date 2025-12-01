package com.example.demoapp.provider_report;

import java.time.LocalDate;

public class ZoneReading {
    public LocalDate date;
    public String zone;       // "Green", "Yellow", "Red"

    public boolean beforeMed; // new
    public boolean afterMed;  // new

    public ZoneReading(LocalDate date, String zone,
                       boolean beforeMed, boolean afterMed) {
        this.date = date;
        this.zone = zone;
        this.beforeMed = beforeMed;
        this.afterMed = afterMed;
    }
}
