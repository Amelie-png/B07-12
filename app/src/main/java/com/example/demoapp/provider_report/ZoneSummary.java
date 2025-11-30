package com.example.demoapp.provider_report;

import java.util.List;
import java.util.Map;

public class ZoneSummary {
    public List<ZoneReading> timeSeries;
    public Map<String, Integer> zoneCounts;

    public ZoneSummary(List<ZoneReading> timeSeries, Map<String, Integer> zoneCounts) {
        this.timeSeries = timeSeries;
        this.zoneCounts = zoneCounts;
    }
}
