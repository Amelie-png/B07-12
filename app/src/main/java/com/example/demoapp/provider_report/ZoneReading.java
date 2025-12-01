package com.example.demoapp.provider_report;

import java.time.LocalDate;

public class ZoneReading {
    public LocalDate date;
    public String zone; // "Green", "Yellow", "Red"

    public ZoneReading(LocalDate date, String zone) {
        this.date = date;
        this.zone = zone;
    }
}
