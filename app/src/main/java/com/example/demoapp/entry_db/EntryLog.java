package com.example.demoapp.entry_db;

import com.google.firebase.firestore.Exclude;
import com.example.demoapp.med.MedicineEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntryLog {
    private String childUid;
    private String id;
    public List<MedicineEntry> medicineEntries; // 用 public 或者 getter
    public String triageState;
    private ArrayList<CategoryName> symptoms;
    private ArrayList<CategoryName> triggers;
    private String date;
    //either "Parent" or "Child"
    private String recorder;

    // ========== 新增字段用于 Provider Report ==========
    private Long timestamp;  // Unix timestamp for sorting
    private String zone;  // "Green", "Yellow", or "Red"
    private Integer rescueInhaler;  // Number of rescue inhaler uses this day
    private Boolean tookController;  // Whether controller medication was taken
    // =================================================

    public EntryLog() {
        this.childUid = "";
        this.id = "";
        this.symptoms = new ArrayList<>();
        this.triggers = new ArrayList<>();
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        this.recorder = "";
        this.timestamp = System.currentTimeMillis();
        this.zone = "Green";  // Default zone
        this.rescueInhaler = 0;
        this.tookController = false;
    }

    public EntryLog(String childId, ArrayList<CategoryName> symptoms, ArrayList<CategoryName> triggers, String date, String recorder) {
        this.childUid = childId;
        this.id = "";
        this.symptoms = symptoms;
        this.triggers = triggers;
        this.date = date;
        this.recorder = recorder;
        this.timestamp = System.currentTimeMillis();
        this.zone = calculateZoneFromSymptoms(symptoms);  // Auto-calculate zone
        this.rescueInhaler = 0;
        this.tookController = false;
    }

    public String getChildUid() {
        return childUid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //Getters and Setters
    public ArrayList<CategoryName> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(ArrayList<CategoryName> symptoms) {
        this.symptoms = symptoms;
    }

    public ArrayList<CategoryName> getTriggers() {
        return triggers;
    }

    public void setTriggers(ArrayList<CategoryName> triggers) {
        this.triggers = triggers;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRecorder() {
        return recorder;
    }

    public void setRecorder(String r) {
        recorder = r;
    }

    public List<MedicineEntry> getMedicineEntries() {
        return medicineEntries;
    }

    public String getTriageState() {
        return triageState;
    }

    // ========== 新增 Getters/Setters for Provider Report ==========

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Integer getRescueInhaler() {
        // 如果 rescueInhaler 为 null，尝试从 medicineEntries 计算
        if (rescueInhaler == null && medicineEntries != null) {
            int count = 0;
            for (MedicineEntry entry : medicineEntries) {
                if ("rescue".equalsIgnoreCase(entry.getMedType())) {
                    count++;
                }
            }
            return count;
        }
        return rescueInhaler != null ? rescueInhaler : 0;
    }

    public void setRescueInhaler(Integer rescueInhaler) {
        this.rescueInhaler = rescueInhaler;
    }

    public Boolean getTookController() {
        // 如果 tookController 为 null，尝试从 medicineEntries 计算
        if (tookController == null && medicineEntries != null) {
            for (MedicineEntry entry : medicineEntries) {
                if ("controller".equalsIgnoreCase(entry.getMedType())) {
                    return true;
                }
            }
            return false;
        }
        return tookController != null ? tookController : false;
    }

    public void setTookController(Boolean tookController) {
        this.tookController = tookController;
    }

    // =============================================================

    @Exclude
    public String getTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date(date));
    }

    @Exclude
    public String getDateTime() {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return dateTimeFormat.format(new Date(date));
    }

    // ========== 辅助方法：根据症状自动计算 Zone ==========
    @Exclude
    private String calculateZoneFromSymptoms(ArrayList<CategoryName> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) {
            return "Green";
        }

        int symptomCount = symptoms.size();
        boolean hasSevereSymptom = false;

        // 检查是否有严重症状
        for (CategoryName symptom : symptoms) {
            String name = symptom.getCategory().toLowerCase();
            if (name.contains("severe") || name.contains("emergency") ||
                    name.contains("shortness of breath")) {
                hasSevereSymptom = true;
                break;
            }
        }

        // 简单的 zone 计算逻辑
        if (hasSevereSymptom || symptomCount >= 4) {
            return "Red";
        } else if (symptomCount >= 2) {
            return "Yellow";
        } else {
            return "Green";
        }
    }

    // ========== 辅助方法：更新药物相关字段 ==========
    @Exclude
    public void updateMedicineFields() {
        if (medicineEntries != null && !medicineEntries.isEmpty()) {
            int rescueCount = 0;
            boolean hasController = false;

            for (MedicineEntry entry : medicineEntries) {
                if ("rescue".equalsIgnoreCase(entry.getMedType())) {
                    rescueCount++;
                } else if ("controller".equalsIgnoreCase(entry.getMedType())) {
                    hasController = true;
                }
            }

            this.rescueInhaler = rescueCount;
            this.tookController = hasController;
        }
    }
}