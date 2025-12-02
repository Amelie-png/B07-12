package com.example.demoapp.med;

import java.util.Map;

public class RescueMed extends Medicine {

    public RescueMed() {
        super();
        setType("rescue");
    }

    public static RescueMed fromMap(Map<String,Object> map) {
        RescueMed r = new RescueMed();
        if (map == null) return r;

        r.setChildId((String) map.get("childId"));
        r.setPurchaseDate((String) map.get("purchaseDate"));
        r.setExpiryDate((String) map.get("expiryDate"));
        r.setCurrentAmount(map.get("currentAmount") instanceof Long ? ((Long) map.get("currentAmount")).intValue() : (Integer) map.get("currentAmount"));
        r.setTotalAmount(map.get("totalAmount") instanceof Long ? ((Long) map.get("totalAmount")).intValue() : (Integer) map.get("totalAmount"));
        r.setLowStockFlag(map.get("lowStockFlag") != null && (Boolean) map.get("lowStockFlag"));
        r.setFlagAuthor((String) map.get("flagAuthor"));

        return r;
    }
}

