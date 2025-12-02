package com.example.demoapp.med;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//Class of individual packages of Medicine
public class MedicineEntry {
    //ID
    private String entryId;
    private String childId;
    //Entry info
    private String medType;
    private int doseCount;
    public long timestamp;
    private boolean flaggedLowStock;
    private String logAuthor;
    //Technique helper
    private boolean techniqueCompleted;
    private int preBreathRating;
    private int postBreathRating;
    private String conditionChange;

    //Constructor
    public MedicineEntry() {} //firebase

    public Map<String,Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        m.put("childId", childId);
        m.put("medType", medType);
        m.put("doseCount", doseCount);
        m.put("flaggedLowStock", flaggedLowStock);
        m.put("techniqueCompleted", techniqueCompleted);
        m.put("preBreathRating", preBreathRating);
        m.put("postBreathRating", postBreathRating);
        m.put("conditionChange", conditionChange);
        m.put("logAuthor", logAuthor);
        m.put("timestamp", timestamp);
        return m;
    }

    //Setter
    public void setEntryId(String entryId) { this.entryId = entryId; }
    public void setChildId(String childId) { this.childId = childId; }
    public void setMedType(String medType) { this.medType = medType; }
    public void setTimestamp(long timestamp){ this.timestamp = timestamp; }
    public void setDoseCount(int doseCount) { this.doseCount = doseCount; }
    public void setFlaggedLowStock(boolean flaggedLowStock) { this.flaggedLowStock = flaggedLowStock; }
    public void setTechniqueCompleted(List<Boolean> techniqueSteps){
        techniqueCompleted = techniqueSteps != null;
        for (boolean b : techniqueSteps) {
            if (!b) {
                techniqueCompleted = false;
                break;
            }
        }
    }
    public void setPreBreathRating(int preBreathRating) { this.preBreathRating = preBreathRating; }
    public void setPostBreathRating(int postBreathRating) { this.postBreathRating = postBreathRating; }
    public void setConditionChange(String conditionChange) { this.conditionChange = conditionChange; }
    public void setLogAuthor(String logAuthor) { this.logAuthor = logAuthor; }

    //Getter
    public String getEntryId() { return entryId; }
    public String getChildId() { return childId; }
    public String getMedType() { return medType; }
    public int getDoseCount() { return doseCount; }
    public long getTimestampValue() { return timestamp; }
    public String getTimestamp() {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return dateTimeFormat.format(new Date(timestamp));
    }
    public int getPreBreathRating() { return preBreathRating; }
    public int getPostBreathRating() { return postBreathRating; }
    public String getConditionChange() { return conditionChange; }
    public boolean isTechniqueCompleted() { return techniqueCompleted; }

    //Utils
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MedicineEntry)) return false;

        MedicineEntry other = (MedicineEntry) obj;

        return doseCount == other.doseCount
                && medType.equals(other.medType)
                && timestamp == other.timestamp;
    }
}
