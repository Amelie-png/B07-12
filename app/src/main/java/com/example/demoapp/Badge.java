package com.example.demoapp;

import java.util.HashMap;
import java.util.Map;

public class Badge {
    public int iconResId;
    private String childId;
    private long controllerStreak;
    private long techniqueStreak;
    private long lastCheckedDate; //for controller
    private long lastTechniqueDate; //for technique
    private boolean firstPerfectWeek;
    private boolean firstTenGoodTechnique;
    private String monthRank;
    private boolean lowRescueMonth;
    private int rescueThreshold;

    public Badge() {} //Firebase

    public Badge(int iconResId) {
        this.iconResId = iconResId;
    }

    public Badge(String childId,
                 long controllerStreak,
                 long techniqueStreak,
                 long lastCheckedDate,
                 long lastTechniqueDate,
                 boolean firstPerfectWeek,
                 boolean firstTenGoodTechnique,
                 String monthRank,
                 boolean lowRescueMonth,
                 int rescueThreshold){
        this.childId = childId;
        this.controllerStreak = controllerStreak;
        this.techniqueStreak = techniqueStreak;
        this.lastCheckedDate = lastCheckedDate;
        this.lastTechniqueDate = lastTechniqueDate;
        this.firstPerfectWeek = firstPerfectWeek;
        this.firstTenGoodTechnique = firstTenGoodTechnique;
        this.monthRank = monthRank;
        this.lowRescueMonth = lowRescueMonth;
        this.rescueThreshold = rescueThreshold;
    }

    public static Badge fromMap(Map<String, Object> map) {
        if (map == null) return new Badge();

        Badge b = new Badge();

        b.controllerStreak = getLong(map, "controllerStreak", 0);
        b.techniqueStreak = getLong(map, "techniqueStreak", 0);

        b.lastCheckedDate = getLong(map, "lastCheckedDate", 0);
        b.lastTechniqueDate = getLong(map, "lastTechniqueDate", 0);

        b.firstPerfectWeek = getBoolean(map, "firstPerfectWeek", false);
        b.firstTenGoodTechnique = getBoolean(map, "firstTenGoodTechnique", false);
        b.lowRescueMonth = getBoolean(map, "lowRescueMonth", false);

        //b.rescueThreshold = getInteger(map, "rescueThreshold", 0);

        return b;
    }

    public static Map<String, Object> toMap(Badge b) {
        Map<String, Object> map = new HashMap<>();

        map.put("controllerStreak", b.controllerStreak);
        map.put("techniqueStreak", b.techniqueStreak);

        map.put("lastCheckedDate", b.lastCheckedDate);
        map.put("lastTechniqueDate", b.lastTechniqueDate);

        map.put("firstPerfectWeek", b.firstPerfectWeek);
        map.put("firstTenGoodTechnique", b.firstTenGoodTechnique);
        map.put("lowRescueMonth", b.lowRescueMonth);

        map.put("rescueThreshold", b.rescueThreshold);

        return map;
    }

    private static long getLong(Map<String, Object> map, String key, long def) {
        Object val = map.get(key);
        return val instanceof Long ? (Long) val : def;
    }

    private static boolean getBoolean(Map<String, Object> map, String key, boolean def) {
        Object val = map.get(key);
        return val instanceof Boolean ? (Boolean) val : def;
    }

    //Getters
    public long getControllerStreak() { return controllerStreak; }
    public long getTechniqueStreak() { return techniqueStreak; }
    public long getLastCheckedDate() { return lastCheckedDate; }
    public long getLastTechniqueDate() { return lastTechniqueDate; }
    public boolean isFirstPerfectWeek() { return firstPerfectWeek; }
    public boolean isFirstTenGoodTechnique() { return firstTenGoodTechnique; }
    public String getMonthRank() { return monthRank; }
    public boolean isLowRescueMonth() { return lowRescueMonth; }
    public int getRescueThreshold() { return rescueThreshold; }

    //Setters

    public void setChildId(String childId) { this.childId = childId; }
    public void setControllerStreak(long controllerStreak) { this.controllerStreak = controllerStreak; }
    public void setTechniqueStreak(long techniqueStreak) { this.techniqueStreak = techniqueStreak; }
    public void setLastCheckedDate(long lastCheckedDate) { this.lastCheckedDate = lastCheckedDate; }
    public void setLastTechniqueDate(long lastTechniqueDate) { this.lastTechniqueDate = lastTechniqueDate; }
    public void setFirstPerfectWeek(boolean firstPerfectWeek) { this.firstPerfectWeek = firstPerfectWeek; }
    public void setFirstTenGoodTechnique(boolean firstTenGoodTechnique) { this.firstTenGoodTechnique = firstTenGoodTechnique; }
    public void setMonthRank(String monthRank) { this.monthRank = monthRank; }
    public void setLowRescueMonth(boolean lowRescueMonth) { this.lowRescueMonth = lowRescueMonth; }
    public void setRescueThreshold(int rescueThreshold) { this.rescueThreshold = rescueThreshold; }
}
