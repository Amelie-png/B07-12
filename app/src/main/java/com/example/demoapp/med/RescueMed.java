package com.example.demoapp.med;

public class RescueMed extends Medicine {

    public RescueMed() {
        super();
        setType("rescue");
    }

    public RescueMed(String childId,
                     int currentAmount,
                     int totalAmount,
                     long purchase,
                     long expiry,
                     boolean lowFlag,
                     int puffsPerDose) {
        super(childId, "rescue", currentAmount, totalAmount, purchase, expiry, lowFlag);
    }
}
