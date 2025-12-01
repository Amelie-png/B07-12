package com.example.demoapp.provider_report;

import java.time.LocalDate;

public class TriageIncident {
    public LocalDate date;
    public String type;
    public String description;

    public TriageIncident(LocalDate date, String type, String description) {
        this.date = date;
        this.type = type;
        this.description = description;
    }
}
