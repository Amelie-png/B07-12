package com.example.demoapp.med;

import java.util.HashMap;
import java.util.Map;

//Class of medicine (controller or rescue)
public abstract class Medicine {
    private String childId;
    private String type;
    private int currentAmount;
    private int totalAmount;
    private String purchaseDate;
    private String expiryDate;
    private boolean lowStockFlag;
    private String flagAuthor;

    //Constructor
    public Medicine() {} //firebase

    public Map<String,Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        m.put("childId", childId);
        m.put("medType", type);
        m.put("currentAmount", currentAmount);
        m.put("totalAmount", totalAmount);
        m.put("purchaseDate", purchaseDate);
        m.put("expiryDate", expiryDate);
        m.put("lowStockFlag", lowStockFlag);
        m.put("flaggedBy", flagAuthor);
        return m;
    }

    //Getters
    public String getChildId() { return childId; }
    public String getType(){ return type; }
    public int getCurrentAmount() { return currentAmount; }
    public int getTotalAmount() { return totalAmount; }
    public boolean isLowStockFlag() { return lowStockFlag; }
    public String getPurchaseDate() { return purchaseDate; }
    public String getExpiryDate() { return expiryDate; }
    public String getFlagAuthor() { return flagAuthor; }

    //Setters
    public void setChildId(String childId) { this.childId = childId; }
    public void setType(String type) { this.type = type; }
    public void setCurrentAmount(int currentAmount) { this.currentAmount = currentAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setLowStockFlag(boolean lowStockFlag) { this.lowStockFlag = lowStockFlag; }
    public void setFlagAuthor(String flagAuthor) { this.flagAuthor = flagAuthor; }
}
