package com.example.demoapp.med;

//Class of medicine (controller or rescue)
public abstract class Medicine {
    private String childId;
    private String type;
    private int currentAmount;
    private int totalAmount;
    private long purchaseDate;
    private long expiryDate;
    private boolean lowStockFlag;

    //Constructor
    public Medicine() {} //firebase
    public Medicine(String childId, String type, int currentAmount, int totalAmount, long purchaseDate, long expiryDate, boolean lowStockFlag) {
        this.childId = childId;
        this.type = type;
        this.currentAmount = currentAmount;
        this.totalAmount = totalAmount;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.lowStockFlag = lowStockFlag;
    }

    //Getters
    public String getChildId() { return childId; }
    public String getType(){ return type; }
    public int getCurrentAmount() { return currentAmount; }
    public int getTotalAmount() { return totalAmount; }
    public boolean isLowStockFlag() { return lowStockFlag; }
    public long getPurchaseDate() { return purchaseDate; }
    public long getExpiryDate() { return expiryDate; }

    //Setters
    public void setUserId(String userId) { this.childId = childId; }
    public void setType(String type) { this.type = type; }
    public void setCurrentAmount(int currentAmount) { this.currentAmount = currentAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public void setPurchaseDate(long purchaseDate) { this.purchaseDate = purchaseDate; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }
    public void setLowStockFlag(boolean lowStockFlag) { this.lowStockFlag = lowStockFlag; }
}
