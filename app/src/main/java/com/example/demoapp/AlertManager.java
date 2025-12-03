package com.example.demoapp;

import com.example.demoapp.med.MedicineEntry;
import com.example.demoapp.med.RescueMed;

import java.util.List;

/*
TODO: add to main activity
AlertManager.getInstance().setListener((id, msg) -> {
    new AlertDialog.Builder(requireContext())
        .setTitle("Alert")
        .setMessage(msg)
        .setPositiveButton("OK", null)
        .show();
});

 */

public class AlertManager {

    private static AlertManager instance;

    private AlertManager() {}

    public static synchronized AlertManager getInstance() {
        if (instance == null) instance = new AlertManager();
        return instance;
    }

    public interface AlertListener {
        void onAlertRaised(String alertId, String message);
    }

    private AlertListener listener;

    public void setListener(AlertListener l) {
        this.listener = l;
    }

    private void emit(String id, String msg) {
        if (listener != null) listener.onAlertRaised(id, msg);
    }

    // Called when logs are fetched or when a new log is added
    public void evaluateAlerts(List<MedicineEntry> logs, RescueMed rescueMed) {

        checkRapidRescueRepeats(logs);
        checkWorseAfterDose(logs);
    }

    private void checkRapidRescueRepeats(List<MedicineEntry> logs) {
        long now = System.currentTimeMillis();
        long threeHoursAgo = now - 3 * 60 * 60 * 1000;

        int rescueCount = 0;

        for (MedicineEntry e : logs) {
            if ("rescue".equals(e.getMedType()) &&
                    e.getTimestampValue() >= threeHoursAgo) {
                rescueCount++;
            }
        }

        if (rescueCount >= 3) {
            emit("rapid_rescue", "Warning: 3+ rescue uses in 3 hours.");
        }
    }

    private void checkWorseAfterDose(List<MedicineEntry> logs) {
        for (MedicineEntry e : logs) {
            if ("rescue".equals(e.getMedType()) &&
                    e.getConditionChange().equals("worse")) {

                emit("worse_after_dose",
                        "Breathing worsened after last rescue dose.");
                return;
            }
        }
    }
}

