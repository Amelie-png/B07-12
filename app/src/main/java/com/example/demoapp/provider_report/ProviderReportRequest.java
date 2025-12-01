package com.example.demoapp.provider_report;

import java.time.LocalDate;

public class ProviderReportRequest {
    public String childId;
    public LocalDate startDate;
    public LocalDate endDate;

    public ProviderReportRequest(String childId, LocalDate startDate, LocalDate endDate) {
        this.childId = childId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
