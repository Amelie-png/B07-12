package com.example.demoapp.med;

public class FilterState {
    public String medType = null;        // "controller" or "rescue" or null
    public Long dateFrom = null;         // epoch millis
    public Long dateTo = null;           // epoch millis

    public boolean hasAnyFilter() {
        return medType != null || dateFrom != null || dateTo != null;
    }

    public void clear() {
        medType = null;
        dateFrom = null;
        dateTo = null;
    }
}