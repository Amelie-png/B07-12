package com.example.demoapp.med;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerMed extends Medicine {
    private String startDate;
    private int dosePerDay;
    private List<String> scheduleDays = new ArrayList<>();

    public ControllerMed() {
        super(); // Firestore requires empty constructor
        setType("controller");
    }

    @Override
    public Map<String,Object> toMap() {
        Map<String,Object> m = super.toMap();;
        m.put("startDate", startDate);
        m.put("dosePerDay", dosePerDay);
        m.put("scheduleDays", scheduleDays);
        return m;
    }

    //Getters
    public String getStartDate() { return startDate; }
    public int getDosePerDay() { return dosePerDay; }
    public List<String> getScheduleDays() { return scheduleDays; }

    //Setters
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setDosePerDay(int dosePerDay) { this.dosePerDay = dosePerDay; }
    public void setScheduleDays(List<String> scheduleDays){ this.scheduleDays = scheduleDays; }
}
