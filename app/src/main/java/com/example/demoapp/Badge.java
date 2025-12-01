package com.example.demoapp;

public class Badge {
    public int iconResId;
    private String childId;
    private int controllerStreak;
    private int techniqueStreak;
    private long lastCheckedDate;
    private boolean firstPerfectWeek;
    private boolean firstTenGoodTechnique;
    private String monthRank;
    private boolean lowRescueMonth;
    private int rescueThreshold;


    public Badge(int iconResId) {
        this.iconResId = iconResId;
    }

    public Badge(String childId,
                 int controllerStreak,
                 int techniqueStreak,
                 long lastCheckedDate,
                 boolean firstPerfectWeek,
                 boolean firstTenGoodTechnique,
                 String monthRank,
                 boolean lowRescueMonth,
                 int rescueThreshold){
        this.childId = childId;
        this.controllerStreak = controllerStreak;
        this.techniqueStreak = techniqueStreak;
        this.lastCheckedDate = lastCheckedDate;
        this.firstPerfectWeek = firstPerfectWeek;
        this.firstTenGoodTechnique = firstTenGoodTechnique;
        this.monthRank = monthRank;
        this.lowRescueMonth = lowRescueMonth;
        this.rescueThreshold = rescueThreshold;
    }

    //Getters
    public int getControllerStreak() { return controllerStreak; }
    public int getTechniqueStreak() { return techniqueStreak; }
    public long getLastCheckedDate() { return lastCheckedDate; }
    public boolean isFirstPerfectWeek() { return firstPerfectWeek; }
    public boolean isFirstTenGoodTechnique() { return firstTenGoodTechnique; }
    public String getMonthRank() { return monthRank; }
    public boolean isLowRescueMonth() { return lowRescueMonth; }
    public int getRescueThreshold() { return rescueThreshold; }

    //Setters

    public void setChildId(String childId) { this.childId = childId; }
    public void setControllerStreak(int controllerStreak) { this.controllerStreak = controllerStreak; }
    public void setTechniqueStreak(int techniqueStreak) { this.techniqueStreak = techniqueStreak; }
    public void setLastCheckedDate(long lastCheckedDate) { this.lastCheckedDate = lastCheckedDate; }
    public void setFirstPerfectWeek(boolean firstPerfectWeek) { this.firstPerfectWeek = firstPerfectWeek; }
    public void setFirstTenGoodTechnique(boolean firstTenGoodTechnique) { this.firstTenGoodTechnique = firstTenGoodTechnique; }
    public void setMonthRank(String monthRank) { this.monthRank = monthRank; }
    public void setLowRescueMonth(boolean lowRescueMonth) { this.lowRescueMonth = lowRescueMonth; }
    public void setRescueThreshold(int rescueThreshold) { this.rescueThreshold = rescueThreshold; }
}
