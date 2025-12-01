package com.example.demoapp.provider_report;

import com.google.firebase.firestore.*;
import java.time.*;
import java.util.*;

public class ZoneReportModule {

    private FirebaseFirestore db;

    public ZoneReportModule(FirebaseFirestore firestore) {
        this.db = firestore;
    }

    public ZoneSummary getZoneSummary(String childId, LocalDate start, LocalDate end) {

        List<ZoneReading> readings = fetchZoneReadings(childId, start, end);

        Map<String, Integer> counts = computeZoneCounts(readings);

        return new ZoneSummary(readings, counts);
    }

    private List<ZoneReading> fetchZoneReadings(String childId, LocalDate start, LocalDate end) {

        List<ZoneReading> list = new ArrayList<>();

        long startMillis = start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = end.atTime(23, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

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
                if (zone == null) zone = "UNKNOWN";

                // NEW — Medicine timing flags
                boolean beforeMed = doc.contains("beforeMed") && Boolean.TRUE.equals(doc.getBoolean("beforeMed"));
                boolean afterMed = doc.contains("afterMed") && Boolean.TRUE.equals(doc.getBoolean("afterMed"));

                list.add(new ZoneReading(date, zone, beforeMed, afterMed));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        list.sort(Comparator.comparing(z -> z.date));

        return list;
    }

    private Map<String, Integer> computeZoneCounts(List<ZoneReading> readings) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("GREEN", 0);
        counts.put("YELLOW", 0);
        counts.put("RED", 0);

        for (ZoneReading r : readings) {
            String z = r.zone.toUpperCase();
            if (counts.containsKey(z)) {
                counts.put(z, counts.get(z) + 1);
            }
        }

        return counts;
    }
}
