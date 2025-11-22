package com.example.demoapp;
public class Child {
    private String name;
    private String dob;
    private int avatarRes;  // 头像资源 ID
    private String notes;

    public Child(String name, String dob, int avatarRes, String notes) {
        this.name = name;
        this.dob = dob;
        this.avatarRes = avatarRes;
        this.notes = notes;
    }

    // Getter 和 Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public int getAvatarRes() { return avatarRes; }
    public void setAvatarRes(int avatarRes) { this.avatarRes = avatarRes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
