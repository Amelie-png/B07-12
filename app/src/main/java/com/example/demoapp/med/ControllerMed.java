package com.example.demoapp.med;

import java.util.ArrayList;
import java.util.List;

public class ControllerMed extends Medicine {
    private long startDate;
    private int dosePerDay;
    private List<String> scheduleDays = new ArrayList<>();

    public ControllerMed() {
        super(); // Firestore requires empty constructor
        setType("controller");
    }

    public ControllerMed(String childId,
                         int currentAmount,
                         int totalAmount,
                         long purchaseDate,
                         long expiryDate,
                         boolean lowStockFlag,
                         long startDate,
                         int dosePerDay,
                         List<String> scheduleDays) {
        super(childId, "controller", currentAmount, totalAmount, purchaseDate, expiryDate, lowStockFlag);
        this.startDate = startDate;
        this.dosePerDay = dosePerDay;
        this.scheduleDays = scheduleDays;
    }

    //Getters

    public long getStartDate() { return startDate; }

    public int getDosePerDay() { return dosePerDay; }
    public List<String> getScheduleDays() { return scheduleDays; }

    //Setters
    public void setStartDate(long startDate) { this.startDate = startDate; }
    public void setDosePerDay(int dosePerDay) { this.dosePerDay = dosePerDay; }
    public void setScheduleDays(List<String> scheduleDays){ this.scheduleDays = scheduleDays; }
}
