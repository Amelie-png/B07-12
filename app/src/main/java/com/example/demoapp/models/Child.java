package com.example.demoapp.models;

import java.util.HashMap;
import java.util.Map;

public class Child {
    private String name;
    private String dob;
    private String parentId;
    private String notes;
    private Map<String, Boolean> sharing;  // 存储哪些数据共享给 provider

    public Child() {} // Firestore 需要空构造函数

    public Child(String name, String dob, String parentId, String notes) {
        this.name = name;
        this.dob = dob;
        this.parentId = parentId;
        this.notes = notes;
        this.sharing = new HashMap<>();
        // 默认全部共享为 false
        this.sharing.put("symptoms", false);
        this.sharing.put("medicines", false);
        this.sharing.put("pef", false);
        this.sharing.put("triage", false);
    }

    // getters 和 setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Map<String, Boolean> getSharing() { return sharing; }
    public void setSharing(Map<String, Boolean> sharing) { this.sharing = sharing; }
}
