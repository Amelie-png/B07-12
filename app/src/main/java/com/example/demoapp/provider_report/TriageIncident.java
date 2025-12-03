package com.example.demoapp.provider_report;

import java.time.LocalDate;

public class TriageIncident {
    public LocalDate date;        // 私有访问
    public  String type;          // 私有访问 (以前是 description)
    public  String description;   // 私有访问 (以前是 details)

    public TriageIncident(LocalDate date, String type, String description) {
        this.date = date;
        this.type = type;
        this.description = description;
    }

    // Getter methods: 必须使用这些方法来获取值
    public LocalDate getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}