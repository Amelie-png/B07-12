package com.example.demoapp.provider_report;
import com.google.firebase.firestore.*;
import java.time.*;
import java.util.*;

public class TriageReportModule {

    private FirebaseFirestore db;

    public TriageReportModule(FirebaseFirestore firestore) {
        this.db = firestore;
    }

    public List<TriageIncident> getTriageIncidents(
            String childId,
            LocalDate start,
            LocalDate end
    ) {
        List<TriageIncident> incidents = new ArrayList<>();

        incidents.addAll(fetchTriageDocuments(childId, start, end));
        incidents.addAll(autoDetectZoneIncidents(childId, start, end));

        incidents.sort(Comparator.comparing(i -> i.date));

        return incidents;
    }

    // -------------------------------------------------------------
    // Fetch explicit triage entries
    // -------------------------------------------------------------
    private List<TriageIncident> fetchTriageDocuments(String childId, LocalDate start, LocalDate end) {
        List<TriageIncident> list = new ArrayList<>();

        long startMillis = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = end.atTime(23,59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        try {
            QuerySnapshot snap = db.collection("triage")
                    .whereEqualTo("childId", childId)
                    .whereGreaterThanOrEqualTo("timestamp", startMillis)
                    .whereLessThanOrEqualTo("timestamp", endMillis)
                    .get().getResult();

            for (DocumentSnapshot doc : snap.getDocuments()) {
                Long ts = doc.getLong("timestamp");
                if (ts == null) continue;

                LocalDate date = Instant.ofEpochMilli(ts)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                String severity = doc.getString("severity");
                int pef = doc.getLong("pef") != null ? doc.getLong("pef").intValue() : -1;

                list.add(new TriageIncident(
                        date,
                        "Triage Entry (" + severity + ")",
                        "Severity: " + severity + ", PEF: " + pef
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // -------------------------------------------------------------
    // Detect red/yellow zone from zone collection
    // -------------------------------------------------------------
    private List<TriageIncident> autoDetectZoneIncidents(String childId, LocalDate start, LocalDate end) {

        List<TriageIncident> list = new ArrayList<>();

        long startMillis = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = end.atTime(23,59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Map<LocalDate, String> zones = new HashMap<>();

        try {
            QuerySnapshot snap = db.collection("zone")
                    .whereEqualTo("childId", childId)
                    .whereGreaterThanOrEqualTo("timestamp", startMillis)
                    .whereLessThanOrEqualTo("timestamp", endMillis)
                    .get().getResult();

            for (DocumentSnapshot doc : snap.getDocuments()) {
                Long ts = doc.getLong("timestamp");
                if (ts == null) continue;

                LocalDate date = Instant.ofEpochMilli(ts)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                String zone = doc.getString("zone");

                zones.put(date, zone);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Detect red zones
        for (LocalDate date : zones.keySet()) {
            if ("RED".equalsIgnoreCase(zones.get(date))) {
                list.add(new TriageIncident(
                        date,
                        "Red Zone Day",
                        "Child was in RED zone on " + date
                ));
            }
        }

        // Detect consecutive YELLOW
        List<LocalDate> sorted = new ArrayList<>(zones.keySet());
        Collections.sort(sorted);

        int streak = 0;

        for (LocalDate d : sorted) {
            if ("YELLOW".equalsIgnoreCase(zones.get(d))) {
                streak++;
                if (streak >= 2) {
                    list.add(new TriageIncident(
                            d,
                            "Persistent Yellow Zone",
                            "Two or more consecutive yellow days"
                    ));
                }
            } else {
                streak = 0;
            }
        }

        return list;
    }
}
