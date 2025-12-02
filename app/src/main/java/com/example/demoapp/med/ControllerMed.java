package com.example.demoapp.med;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ControllerMed extends Medicine {
    private String startDate; // ISO string
    private int dosePerDay;
    private List<String> scheduleDays = new ArrayList<>();

    public ControllerMed() {
        super();
        setType("controller");
    }

    @Override
    public Map<String,Object> toMap() {
        Map<String,Object> m = super.toMap();
        m.put("startDate", startDate);
        m.put("dosePerDay", dosePerDay);
        m.put("scheduleDays", scheduleDays);
        return m;
    }

    public static ControllerMed fromMap(Map<String, Object> map) {
        ControllerMed c = new ControllerMed();
        if (map == null) return c;

        c.setChildId((String) map.get("childId"));
        c.setPurchaseDate((String) map.get("purchaseDate"));
        c.setExpiryDate((String) map.get("expiryDate"));
        c.setStartDate((String) map.get("startDate"));
        c.setCurrentAmount(map.get("currentAmount") instanceof Long ? ((Long) map.get("currentAmount")).intValue() : (Integer) map.get("currentAmount"));
        c.setTotalAmount(map.get("totalAmount") instanceof Long ? ((Long) map.get("totalAmount")).intValue() : (Integer) map.get("totalAmount"));
        c.setDosePerDay(map.get("dosePerDay") instanceof Long ? ((Long) map.get("dosePerDay")).intValue() : (Integer) map.get("dosePerDay"));
        c.setScheduleDays((List<String>) map.get("scheduleDays"));
        c.setLowStockFlag(map.get("lowStockFlag") != null && (Boolean) map.get("lowStockFlag"));
        c.setFlagAuthor((String) map.get("flagAuthor"));

        return c;
    }

    // Getters/Setters
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public int getDosePerDay() { return dosePerDay; }
    public void setDosePerDay(int dosePerDay) { this.dosePerDay = dosePerDay; }
    public List<String> getScheduleDays() { return scheduleDays; }
    public void setScheduleDays(List<String> scheduleDays) { this.scheduleDays = scheduleDays; }
}
