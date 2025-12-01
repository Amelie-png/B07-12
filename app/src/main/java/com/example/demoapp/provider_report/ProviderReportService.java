package com.example.demoapp.provider_report;

import com.example.demoapp.provider_report.ProviderReportRequest;
import com.example.demoapp.provider_report.ProviderReportResult;
import com.example.demoapp.provider_report.ZoneReportModule;
import com.example.demoapp.provider_report.TriageReportModule;

public class ProviderReportService {

    private ZoneReportModule zoneModule;
    private TriageReportModule triageModule;

    public ProviderReportService(ZoneReportModule zoneModule, TriageReportModule triageModule) {
        this.zoneModule = zoneModule;
        this.triageModule = triageModule;
    }

    public ProviderReportResult buildReport(ProviderReportRequest request) {

        return new ProviderReportResult(
                zoneModule.getZoneSummary(
                        request.childId,
                        request.startDate,
                        request.endDate
                ),
                triageModule.getTriageIncidents(
                        request.childId,
                        request.startDate,
                        request.endDate
                )
        );
    }
}
