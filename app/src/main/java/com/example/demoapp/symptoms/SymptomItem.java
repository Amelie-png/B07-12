package com.example.demoapp.symptoms;

public class SymptomItem {
    private String name;
    private boolean selected;

    public SymptomItem(String name) {
        this.name = name;
        this.selected = false;
    }

    public String getName() { return name; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}

