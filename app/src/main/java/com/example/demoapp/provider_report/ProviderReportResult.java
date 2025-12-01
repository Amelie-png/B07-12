package com.example.demoapp.provider_report;

import com.example.demoapp.provider_report.ZoneSummary;
import com.example.demoapp.provider_report.TriageIncident;

import java.util.List;

public class ProviderReportResult {
    public ZoneSummary zoneSummary;
    public List<TriageIncident> triageIncidents;

    public ProviderReportResult(ZoneSummary zoneSummary, List<TriageIncident> triageIncidents) {
        this.zoneSummary = zoneSummary;
        this.triageIncidents = triageIncidents;
    }
}
