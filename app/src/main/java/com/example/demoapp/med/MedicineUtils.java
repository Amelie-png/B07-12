package com.example.demoapp.med;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MedicineUtils {

    /*
     * Calculates adherence as:
     *   (number of controller doses actually taken) / (number expected)
     * Expected = dosesPerDay * number of days between start–end (inclusive)
     * -------- For adherence summary for parents/providers ----------
     */
    public static double calculateControllerAdherence(List<MedicineEntry> allEntries,
                                                      ControllerMed controllerConfig,
                                                      long startEpoch,
                                                      long endEpoch) {

        if (controllerConfig == null || controllerConfig.getDosePerDay() <= 0) return 0;

        // Count entries in date range where type = controller
        int taken = 0;
        for (MedicineEntry e : allEntries) {
            if ("controller".equals(e.getMedType())
                    && e.getTimestampValue() >= startEpoch
                    && e.getTimestampValue() <= endEpoch) {
                taken++;
            }
        }

        long days = calculateDaysBetween(startEpoch, endEpoch);
        long expected = days * controllerConfig.getDosePerDay();

        if (expected == 0) return 0;

        return (double) taken / (double) expected;
    }

    //TODO
    public static int updateControllerStreak(){
        return 0;
    }

    public static int updateTechniqueStreak(){
        return 0;
    }

    /*
     * Count rescue usage in a given time range.
     * ------------ For rescue count in last hours ------------
     */
    public static int countRescueUsage(List<MedicineEntry> entries, long startEpoch, long endEpoch) {
        int count = 0;
        for (MedicineEntry e : entries) {
            if ("rescue".equals(e.getMedType())
                    && e.getTimestampValue() >= startEpoch
                    && e.getTimestampValue() <= endEpoch) {
                count++;
            }
        }
        return count;
    }

    /*
     * Returns timestamp of last rescue usage, or -1 if none.
     * ----------- For last rescue time data on home page -------------
     */
    public static long getLastRescueTime(List<MedicineEntry> entries) {
        long latest = -1;
        for (MedicineEntry e : entries) {
            if ("rescue".equals(e.getMedType())) {
                if (e.getTimestampValue() > latest) {
                    latest = e.getTimestampValue();
                }
            }
        }
        return latest;
    }

    /*
     * Returns rescue count starting from now going back given days;
     * ----------- For 7-day/30-day trend on home page ---------------
     */
    public static int getWeeklyRescueCount(List<MedicineEntry> entries, int days) {
        long now = System.currentTimeMillis();
        long daysAgo = now - (days * 24L * 60L * 60L * 1000L);
        return countRescueUsage(entries, daysAgo, now);
    }

    /*
     * Helper: calculate days between timestamps (rounded up)
     */
    private static long calculateDaysBetween(long start, long end) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTimeInMillis(start);
        c2.setTimeInMillis(end);

        long diff = c2.getTimeInMillis() - c1.getTimeInMillis();
        long days = diff / (24L * 60L * 60L * 1000L);

        return Math.max(1, days + 1); // inclusive range
    }

    public static boolean isSameDay(long t1, long t2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTimeInMillis(t1);
        c2.setTimeInMillis(t2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isYesterday(long previousDay, long today) {
        Calendar cPrev = Calendar.getInstance();
        Calendar cToday = Calendar.getInstance();
        cPrev.setTimeInMillis(previousDay);
        cToday.setTimeInMillis(today);

        cToday.add(Calendar.DAY_OF_YEAR, -1);

        return cPrev.get(Calendar.YEAR) == cToday.get(Calendar.YEAR)
                && cPrev.get(Calendar.DAY_OF_YEAR) == cToday.get(Calendar.DAY_OF_YEAR);
    }

}