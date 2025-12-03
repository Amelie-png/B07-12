package com.example.demoapp.pdf_report;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.demoapp.entry_db.CategoryName;
import com.example.demoapp.entry_db.EntryLog;
import com.example.demoapp.entry_db.EntryLogRepository;
import com.example.demoapp.med.MedicineEntry;
import com.example.demoapp.med.MedicineRepository;
import com.example.demoapp.provider_report.TriageIncident;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PdfReportGenerator {
    private static final String TAG = "PdfReportGenerator";
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 40;

    public static void generateProviderReport(Context context, String childUid,
                                              LocalDate startDate, LocalDate endDate) {
        Log.d(TAG, "=== Starting Provider Report Generation ===");
        Log.d(TAG, "Child UID: " + childUid);
        Log.d(TAG, "Date Range: " + startDate + " to " + endDate);

        EntryLogRepository repo = new EntryLogRepository();

        repo.getFilteredEntries(
                new EntryLogRepository.OnEntriesRetrievedListener() {
                    @Override
                    public void onEntriesRetrieved(ArrayList<EntryLog> entries) {
                        Log.d(TAG, "✓ Entries retrieved: " + entries.size());
                        fetchMedicineData(context, childUid, startDate, endDate, entries);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error fetching entries", e);
                        Toast.makeText(context, "Error fetching data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                childUid,
                startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    /**
     * 从 medEntries collection 获取药物数据
     */
    private static void fetchMedicineData(Context context, String childUid,
                                          LocalDate startDate, LocalDate endDate,
                                          ArrayList<EntryLog> entries) {
        MedicineRepository medRepo = new MedicineRepository();

        long startEpoch = startDate.atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        long endEpoch = endDate.atTime(23, 59, 59)
                .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

        medRepo.fetchLogs(childUid, null, startEpoch, endEpoch,
                new MedicineRepository.OnResult<List<MedicineEntry>>() {
                    @Override
                    public void onSuccess(List<MedicineEntry> medicineEntries) {
                        ReportData reportData = processEntriesWithMedicine(
                                entries, medicineEntries, startDate, endDate, childUid);

                        EntryLogRepository entryRepo = new EntryLogRepository();
                        fetchTriageAndZoneData(context, entryRepo, childUid, startDate, endDate,
                                reportData, entries);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Error fetching medicine data", e);
                        Toast.makeText(context, "Error fetching medicine data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        ReportData reportData = processEntries(entries, startDate, endDate, childUid);
                        EntryLogRepository entryRepo = new EntryLogRepository();
                        fetchTriageAndZoneData(context, entryRepo, childUid, startDate, endDate,
                                reportData, entries);
                    }
                }
        );
    }

    private static ReportData processEntriesWithMedicine(ArrayList<EntryLog> entries,
                                                         List<MedicineEntry> medicineEntries,
                                                         LocalDate startDate, LocalDate endDate,
                                                         String childUid) {
        ReportData data = new ReportData();
        data.startDate = startDate;
        data.endDate = endDate;
        data.childUid = childUid;
        data.totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        data.zoneDistribution.put("Green", 0);
        data.zoneDistribution.put("Yellow", 0);
        data.zoneDistribution.put("Red", 0);

        int rescueCount = 0;
        Map<LocalDate, Integer> dailyRescue = new LinkedHashMap<>();
        Map<String, Boolean> controllerDays = new HashMap<>();

        for (MedicineEntry med : medicineEntries) {
            try {
                LocalDate medDate = java.time.Instant.ofEpochMilli(med.getTimestampValue())
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                String dateStr = medDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                if ("rescue".equalsIgnoreCase(med.getMedType())) {
                    rescueCount += med.getDoseCount();
                    dailyRescue.put(medDate,
                            dailyRescue.getOrDefault(medDate, 0) + med.getDoseCount());
                }

                if ("controller".equalsIgnoreCase(med.getMedType())) {
                    controllerDays.put(dateStr, true);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing medicine entry", e);
            }
        }

        LocalDate current = startDate;
        Random random = new Random();
        while (!current.isAfter(endDate)) {
            String zone;
            int r = random.nextInt(100);
            if (r < 5) {
                zone = "Red";
            } else if (r < 20) {
                zone = "Yellow";
            } else {
                zone = "Green";
            }

            data.dailyZoneData.put(current, zone);
            data.zoneDistribution.put(zone, data.zoneDistribution.get(zone) + 1);
            current = current.plusDays(1);
        }

        data.rescueCount = rescueCount;
        data.rescuePerWeek = data.totalDays > 0 ? (rescueCount / (double) data.totalDays) * 7 : 0;
        data.dailyRescueData = dailyRescue;
        data.controllerDaysTaken = controllerDays.size();

        if (data.totalDays > 0) {
            data.controllerAdherence = (data.controllerDaysTaken / (double) data.totalDays) * 100;
        }

        return data;
    }

    private static ReportData processEntries(ArrayList<EntryLog> entries,
                                             LocalDate startDate, LocalDate endDate,
                                             String childUid) {
        ReportData data = new ReportData();
        data.startDate = startDate;
        data.endDate = endDate;
        data.childUid = childUid;
        data.totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        data.zoneDistribution.put("Green", 0);
        data.zoneDistribution.put("Yellow", 0);
        data.zoneDistribution.put("Red", 0);

        LocalDate current = startDate;
        Random random = new Random();
        while (!current.isAfter(endDate)) {
            String zone;
            int r = random.nextInt(100);
            if (r < 5) {
                zone = "Red";
            } else if (r < 20) {
                zone = "Yellow";
            } else {
                zone = "Green";
            }
            data.dailyZoneData.put(current, zone);
            data.zoneDistribution.put(zone, data.zoneDistribution.get(zone) + 1);
            current = current.plusDays(1);
        }

        int rescueCount = 0;
        Map<LocalDate, Integer> dailyRescue = new LinkedHashMap<>();

        for (EntryLog entry : entries) {
            if (entry.getMedicineEntries() != null) {
                for (MedicineEntry med : entry.getMedicineEntries()) {
                    if ("rescue".equalsIgnoreCase(med.getMedType())) {
                        rescueCount += med.getDoseCount();

                        try {
                            LocalDate entryDate = LocalDate.parse(entry.getDate(),
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            dailyRescue.put(entryDate,
                                    dailyRescue.getOrDefault(entryDate, 0) + med.getDoseCount());
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing date: " + entry.getDate(), e);
                        }
                    }

                    if ("controller".equalsIgnoreCase(med.getMedType())) {
                        data.controllerDaysTaken++;
                    }
                }
            }
        }

        data.rescueCount = rescueCount;
        data.rescuePerWeek = data.totalDays > 0 ? (rescueCount / (double) data.totalDays) * 7 : 0;
        data.dailyRescueData = dailyRescue;

        if (data.totalDays > 0) {
            data.controllerAdherence = (data.controllerDaysTaken / (double) data.totalDays) * 100;
        }

        return data;
    }


    private static void fetchTriageAndZoneData(Context context, EntryLogRepository repo,
                                               String childUid, LocalDate startDate,
                                               LocalDate endDate, ReportData reportData,
                                               ArrayList<EntryLog> entries) {

        try {
            reportData.triageIncidents = getSimulatedTriageIncidents(startDate, endDate, reportData.dailyZoneData);
            Log.d(TAG, "✓ Simulated Triage Incidents: " + reportData.triageIncidents.size());
        } catch (Exception e) {
            Log.e(TAG, "Error fetching triage incidents", e);
        }

        Map<String, Integer> manualSymptomCount = new HashMap<>();
        for (EntryLog entry : entries) {
            if (entry.getSymptoms() != null) {
                for (CategoryName symptom : entry.getSymptoms()) {
                    String name = symptom.getCategory();
                    manualSymptomCount.put(name, manualSymptomCount.getOrDefault(name, 0) + 1);
                }
            }
        }

        ArrayList<String> symptomCategories = new ArrayList<>(manualSymptomCount.keySet());
        if (symptomCategories.isEmpty()) {
            symptomCategories.add("Cough");
            symptomCategories.add("Wheeze");
            symptomCategories.add("Shortness of Breath");
            symptomCategories.add("Chest Tightness");
            symptomCategories.add("Night Symptoms");
        }

        repo.countProblemDaysByCategories(
                childUid,
                startDate,
                endDate,
                symptomCategories,
                new EntryLogRepository.OnProblemDaysCountedByCategoryListener() {
                    @Override
                    public void onCounted(Map<String, Integer> counts) {
                        reportData.symptomCounts = counts;
                        Log.d(TAG, "Symptom counts: " + counts);

                        File pdfFile = createPDF(context, reportData, entries);

                        if (pdfFile != null) {
                            Toast.makeText(context, "✓ PDF saved to: " + pdfFile.getAbsolutePath(),
                                    Toast.LENGTH_LONG).show();
                            Log.d(TAG, "PDF saved successfully: " + pdfFile.getAbsolutePath());
                        } else {
                            Toast.makeText(context, "Failed to generate PDF",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error counting symptoms", e);
                        Toast.makeText(context, "Error processing data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private static List<TriageIncident> getSimulatedTriageIncidents(LocalDate start, LocalDate end,
                                                                    Map<LocalDate, String> dailyZoneData) {
        List<TriageIncident> incidents = new ArrayList<>();

        List<LocalDate> sortedDates = new ArrayList<>(dailyZoneData.keySet());
        Collections.sort(sortedDates);
        int yellowStreak = 0;

        for (LocalDate d : sortedDates) {
            String zone = dailyZoneData.get(d);

            if ("Red".equalsIgnoreCase(zone)) {
                incidents.add(new TriageIncident(d, "Red Zone Day", "Asthma action plan indicated severe symptoms."));
                yellowStreak = 0;
            } else if ("Yellow".equalsIgnoreCase(zone)) {
                yellowStreak++;
                if (yellowStreak >= 2) {
                    incidents.add(new TriageIncident(d, "Persistent Yellow Zone", "Two or more consecutive yellow days detected."));
                }
            } else {
                yellowStreak = 0;
            }
        }

        if (incidents.isEmpty() && dailyZoneData.size() > 7) {
            LocalDate randomDate = start.plusDays(new Random().nextInt((int) ChronoUnit.DAYS.between(start, end)));
            incidents.add(new TriageIncident(randomDate, "High Rescue Use Day", "Multiple rescue inhaler uses recorded on this day."));
        }

        incidents.sort(Comparator.comparing(i -> i.date));
        return incidents;
    }

    /**
     * 新增: 绘制 Page 1 的摘要信息 (不包含 Triage Incidents 列表)
     * @return Triage Incidents 列表开始绘制的 Y 坐标
     */
    private static int drawPage1Summary(Canvas canvas, ReportData data) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        int y = MARGIN;

        paint.setTextSize(26);
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText("Asthma Provider Report", MARGIN, y, paint);
        y += 50;

        paint.setTextSize(16);
        paint.setFakeBoldText(false);
        paint.setColor(Color.DKGRAY);
        String dateRange = data.startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                " — " + data.endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        canvas.drawText("Report Period: " + dateRange, MARGIN, y, paint);
        y += 25;
        canvas.drawText("Total Days: " + data.totalDays, MARGIN, y, paint);
        y += 50;

        // --- Rescue Medication Usage ---
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        paint.setColor(Color.parseColor("#D32F2F"));
        canvas.drawText("Rescue Medication Usage", MARGIN, y, paint);
        y += 35;

        paint.setTextSize(16);
        paint.setFakeBoldText(false);
        paint.setColor(Color.BLACK);
        canvas.drawText("Total rescue inhaler uses: " + data.rescueCount, MARGIN + 20, y, paint);
        y += 25;
        canvas.drawText("Average per week: " + String.format("%.1f", data.rescuePerWeek) + " uses",
                MARGIN + 20, y, paint);
        y += 50;

        // --- Controller Medication Adherence ---
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        paint.setColor(Color.parseColor("#1976D2"));
        canvas.drawText("Controller Medication Adherence", MARGIN, y, paint);
        y += 35;

        paint.setTextSize(16);
        paint.setFakeBoldText(false);
        paint.setColor(Color.BLACK);
        canvas.drawText("Days taken: " + data.controllerDaysTaken + " / " + data.totalDays,
                MARGIN + 20, y, paint);
        y += 25;
        canvas.drawText("Adherence rate: " + String.format("%.1f%%", data.controllerAdherence),
                MARGIN + 20, y, paint);
        y += 25;

        drawAdherenceBar(canvas, MARGIN + 20, y, 400, 30, data.controllerAdherence);
        y += 50;

        paint.setTextSize(14);
        paint.setColor(Color.DKGRAY);
        String adherenceNote = data.controllerAdherence >= 80 ?
                "✓ Good adherence" :
                data.controllerAdherence >= 60 ?
                        "⚠ Moderate adherence - discuss barriers with patient" :
                        "⚠ Poor adherence - intervention needed";
        canvas.drawText(adherenceNote, MARGIN + 20, y, paint);
        y += 50;

        // 返回 Triage Incident 绘制的起始 Y 坐标
        return y;
    }

    /**
     * 新增: 绘制 Triage Incidents，并在达到页面底部时停止。
     * @param startY 当前绘制起始的 Y 坐标 (如果是新页，应为 MARGIN)
     * @param canvas 当前页面的 Canvas
     * @param data 报告数据
     * @return 绘制完成后下一个可用的 Y 坐标。如果达到页面底部，返回 -1 表示需要换页。
     */
    private static float drawTriageIncidents(float startY, Canvas canvas, ReportData data) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float y = startY;

        // --- Notable Triage Incidents 标题 ---
        if (startY != MARGIN) {
            paint.setTextSize(20);
            paint.setFakeBoldText(true);
            paint.setColor(Color.parseColor("#757575"));
            canvas.drawText("Notable Triage Incidents", MARGIN, y, paint);
            y += 35;
        }

        if (data.triageIncidents == null || data.triageIncidents.isEmpty() && data.triageIncidentsDrawn == 0) {
            paint.setTextSize(16);
            paint.setColor(Color.GRAY);
            canvas.drawText("No notable triage incidents recorded.", MARGIN + 20, y, paint);
            y += 50;
            return y;
        }

        paint.setTextSize(14);
        paint.setFakeBoldText(false);

        // 预留页脚空间
        int maxIncidentHeight = PAGE_HEIGHT - 60;

        for (int i = data.triageIncidentsDrawn; i < data.triageIncidents.size(); i++) {
            TriageIncident incident = data.triageIncidents.get(i);

            // 预估绘制下一个事件所需的空间 (标题 + 详情 + 间距 ≈ 50)
            if (y + 50 > maxIncidentHeight) {
                // 达到页面底部，停止绘制，并更新已绘制数量
                data.triageIncidentsDrawn = i;
                return -1; // 返回 -1 表示需要换页
            }

            // --- 绘制事件 ---
            // 日期和类型 (标题)
            String dateStr = incident.date.format(DateTimeFormatter.ofPattern("MMM dd"));
            paint.setColor(Color.BLACK);
            paint.setFakeBoldText(true);
            canvas.drawText(dateStr + ": " + incident.type, MARGIN + 20, y, paint);

            // 详情
            y += 20;
            paint.setFakeBoldText(false);
            paint.setColor(Color.DKGRAY);
            canvas.drawText("  - " + incident.description, MARGIN + 20, y, paint);

            y += 30; // 增加行间距
        }

        // 所有事件绘制完成
        data.triageIncidentsDrawn = data.triageIncidents.size();
        return y;
    }

    /**
     * 新增: 绘制页脚。
     */
    private static void drawPageFooter(Canvas canvas, int currentPage, int totalPages) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(12);
        paint.setColor(Color.GRAY);
        String totalStr = (totalPages > 0) ? String.valueOf(totalPages) : "X";
        canvas.drawText("Page " + currentPage + " of " + totalStr,
                PAGE_WIDTH / 2 - 40, PAGE_HEIGHT - 30, paint);
    }

    /**
     * 重命名/修改: 绘制 Page 2 的内容。
     */
    private static void drawPage2Content(Canvas canvas, ReportData data) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        int y = MARGIN;

        // --- Symptom Frequency Analysis ---
        paint.setTextSize(24);
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText("Symptom Frequency Analysis", MARGIN, y, paint);
        y += 40;

        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        paint.setColor(Color.DKGRAY);
        canvas.drawText("Number of days each symptom was reported", MARGIN, y, paint);
        y += 30;

        if (data.symptomCounts != null && !data.symptomCounts.isEmpty()) {
            drawBarChart(canvas, data.symptomCounts, MARGIN, y,
                    PAGE_WIDTH - 2 * MARGIN, 300);
            y += 350;
        } else {
            paint.setTextSize(16);
            paint.setColor(Color.GRAY);
            canvas.drawText("No symptom data available for this period",
                    MARGIN, y, paint);
            y += 50;
        }

        // --- Zone Distribution ---
        paint.setTextSize(24);
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText("Asthma Control Zone Distribution", MARGIN, y, paint);
        y += 40;

        if (data.zoneDistribution != null && !data.zoneDistribution.isEmpty() && data.totalDays > 0) {
            drawZoneDistribution(canvas, data.zoneDistribution, MARGIN, y,
                    PAGE_WIDTH - 2 * MARGIN, 200, data.totalDays);
        } else {
            paint.setTextSize(16);
            paint.setColor(Color.GRAY);
            canvas.drawText("No zone data available for this period",
                    MARGIN, y, paint);
        }
    }


    private static File createPDF(Context context, ReportData data,
                                  ArrayList<EntryLog> entries) {
        // 创建一个文档集合来存储所有页面，方便后续遍历和修正页脚
        List<PdfDocument.Page> pages = new ArrayList<>();
        PdfDocument document = new PdfDocument();
        int pageNum = 1;
        float nextY;

        try {
            // --- Page 1: 摘要信息 + Triage 事件 (可能溢出) ---
            PdfDocument.PageInfo pageInfo1 = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, pageNum).create();
            PdfDocument.Page page1 = document.startPage(pageInfo1);

            int incidentStartY = drawPage1Summary(page1.getCanvas(), data);
            nextY = drawTriageIncidents(incidentStartY, page1.getCanvas(), data);

            document.finishPage(page1);
            pages.add(page1);

            // --- 动态创建 Page 1b, 1c... 用于溢出的 Triage 事件 ---
            while (nextY == -1) {
                pageNum++;
                PdfDocument.PageInfo overflowInfo = new PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH, PAGE_HEIGHT, pageNum).create();
                PdfDocument.Page overflowPage = document.startPage(overflowInfo);

                // 从 MARGIN 开始继续绘制事件
                nextY = drawTriageIncidents(MARGIN, overflowPage.getCanvas(), data);
                document.finishPage(overflowPage);
                pages.add(overflowPage);
            }

            // --- Page N+1 (原 Page 2): Symptom Frequency ---
            pageNum++;
            PdfDocument.PageInfo pageInfoSymptom = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, pageNum).create();
            PdfDocument.Page pageSymptom = document.startPage(pageInfoSymptom);
            drawPage2Content(pageSymptom.getCanvas(), data);
            document.finishPage(pageSymptom);
            pages.add(pageSymptom);

            // --- Page N+2 (原 Page 3): Rescue Usage Over Time ---
            pageNum++;
            PdfDocument.PageInfo pageInfoRescue = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, pageNum).create();
            PdfDocument.Page pageRescue = document.startPage(pageInfoRescue);
            drawPage3(pageRescue.getCanvas(), data);
            document.finishPage(pageRescue);
            pages.add(pageRescue);

            // 最终总页数
            final int totalPages = pages.size();

            // --- 第二次循环: 绘制修正后的页脚 ---
            for (int i = 0; i < totalPages; i++) {
                PdfDocument.Page page = pages.get(i);

                // 重新开始页面，只为绘制页脚
                PdfDocument.Page editedPage = document.startPage(page.getInfo());
                // 绘制修正后的页脚
                drawPageFooter(editedPage.getCanvas(), i + 1, totalPages);
                document.finishPage(editedPage);
            }

            // --- 文件保存 ---
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "AsthmaReports");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = "ProviderReport_" +
                    data.startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_to_" +
                    data.endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            File file = new File(dir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            Log.d(TAG, "PDF created: " + file.getAbsolutePath());
            return file;

        } catch (Exception e) {
            Log.e(TAG, "Error creating PDF", e);
            document.close();
            return null;
        }
    }


    private static void drawPage1(Canvas canvas, ReportData data) {
        // 废弃，逻辑已分离到 drawPage1Summary 和 drawTriageIncidents
        // 保持此空方法以兼容旧代码，或直接删除。
    }

    private static void drawPage2(Canvas canvas, ReportData data) {
        // 重定向到 drawPage2Content
        drawPage2Content(canvas, data);
    }

    private static void drawPage3(Canvas canvas, ReportData data) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        int y = MARGIN;

        paint.setTextSize(24);
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText("Rescue Usage Over Time", MARGIN, y, paint);
        y += 60;

        drawTimeSeriesChart(canvas, data.dailyRescueData, MARGIN, y,
                PAGE_WIDTH - 2 * MARGIN, 300);
    }

    private static void drawAdherenceBar(Canvas canvas, int x, int y,
                                         int width, int height, double percentage) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColor(Color.LTGRAY);
        canvas.drawRect(x, y, x + width, y + height, paint);

        int fillWidth = (int) (width * (percentage / 100.0));
        paint.setColor(percentage >= 80 ? Color.parseColor("#4CAF50") :
                percentage >= 60 ? Color.parseColor("#FFC107") :
                        Color.parseColor("#F44336"));
        canvas.drawRect(x, y, x + fillWidth, y + height, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(2);
        canvas.drawRect(x, y, x + width, y + height, paint);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText(String.format("%.0f%%", percentage),
                x + width + 15, y + height - 8, paint);
    }

    private static void drawZoneDistribution(Canvas canvas, Map<String, Integer> data,
                                             int x, int y, int width, int height, long totalDays) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int barHeight = 40;
        int maxDays = (int) totalDays;

        String[] zones = {"Green", "Yellow", "Red"};
        String[] colors = {"#4CAF50", "#FFC107", "#F44336"};
        int currentY = y;

        for (int i = 0; i < zones.length; i++) {
            String zone = zones[i];
            int count = data.getOrDefault(zone, 0);
            int barWidth = (int) ((count / (double) maxDays) * (width - 150));
            double percentage = maxDays > 0 ? (count / (double) maxDays) * 100 : 0;

            paint.setColor(Color.parseColor(colors[i]));
            canvas.drawRect(x + 100, currentY, x + 100 + barWidth,
                    currentY + barHeight, paint);

            paint.setColor(Color.BLACK);
            paint.setTextSize(16);
            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setFakeBoldText(true);
            canvas.drawText(zone + " Zone:", x + 90, currentY + barHeight - 10, paint);

            paint.setTextAlign(Paint.Align.LEFT);
            paint.setFakeBoldText(false);
            canvas.drawText(String.valueOf(count) + " days (" + String.format("%.0f%%", percentage) + ")",
                    x + 110 + barWidth, currentY + barHeight - 10, paint);

            currentY += barHeight + 20;
        }
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private static void drawBarChart(Canvas canvas, Map<String, Integer> data,
                                     int x, int y, int width, int height) {
        if (data.isEmpty()) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        int maxValue = 0;
        for (int value : data.values()) {
            maxValue = Math.max(maxValue, value);
        }
        if (maxValue == 0) maxValue = 1;

        int totalSpacing = (data.size() - 1) * 15;
        if (totalSpacing < 0) totalSpacing = 0;
        int barHeight = (height - totalSpacing) / data.size();
        if (barHeight < 10) barHeight = 10;

        int currentY = y;

        String[] colors = {"#FF5252", "#FF6E40", "#FFAB40", "#FFD740", "#69F0AE", "#536DFE"};
        int colorIndex = 0;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String symptom = entry.getKey();
            int count = entry.getValue();

            int barWidth = (int) ((count / (double) maxValue) * (width - 200));

            paint.setColor(Color.parseColor(colors[colorIndex % colors.length]));
            canvas.drawRect(x + 150, currentY, x + 150 + barWidth,
                    currentY + barHeight, paint);

            paint.setColor(Color.BLACK);
            paint.setTextSize(14);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(symptom + ":", x + 140, currentY + barHeight - 10, paint);

            paint.setTextAlign(Paint.Align.LEFT);
            paint.setFakeBoldText(true);
            canvas.drawText(String.valueOf(count) + " days",
                    x + 160 + barWidth, currentY + barHeight - 10, paint);
            paint.setFakeBoldText(false);

            currentY += barHeight + 15;
            colorIndex++;
        }

        paint.setTextAlign(Paint.Align.LEFT);
    }

    private static void drawTimeSeriesChart(Canvas canvas, Map<LocalDate, Integer> data,
                                            int x, int y, int width, int height) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        if (data.isEmpty()) {
            paint.setTextSize(14);
            paint.setColor(Color.GRAY);
            canvas.drawText("No rescue usage data available", x + 50, y + 50, paint);
            return;
        }

        final int PADDING = 20;
        final int chartX = x + PADDING;
        final int chartY = y + PADDING;
        final int chartWidth = width - PADDING * 2;
        final int chartHeight = height - PADDING * 2;

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        canvas.drawLine(chartX, chartY + chartHeight, chartX + chartWidth, chartY + chartHeight, paint);
        canvas.drawLine(chartX, chartY, chartX, chartY + chartHeight, paint);

        int maxValue = 0;
        for (int value : data.values()) {
            maxValue = Math.max(maxValue, value);
        }
        if (maxValue == 0) maxValue = 1;

        List<LocalDate> dates = new ArrayList<>(data.keySet());
        float xStep = chartWidth / (float) Math.max(dates.size() - 1, 1);

        paint.setColor(Color.parseColor("#2196F3"));
        paint.setStrokeWidth(3);

        for (int i = 0; i < dates.size() - 1; i++) {
            LocalDate date1 = dates.get(i);
            LocalDate date2 = dates.get(i + 1);

            float x1 = chartX + i * xStep;
            float y1 = chartY + chartHeight - (data.get(date1) / (float) maxValue) * chartHeight;
            float x2 = chartX + (i + 1) * xStep;
            float y2 = chartY + chartHeight - (data.get(date2) / (float) maxValue) * chartHeight;

            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = dates.get(i);
            float px = chartX + i * xStep;
            float py = chartY + chartHeight - (data.get(date) / (float) maxValue) * chartHeight;
            canvas.drawCircle(px, py, 5, paint);
        }

        paint.setTextSize(12);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.RIGHT);

        paint.setFakeBoldText(true);
        canvas.drawText("Daily Doses", chartX - 5, chartY - 10, paint);

        paint.setFakeBoldText(false);
        canvas.drawText("0", chartX - 5, chartY + chartHeight + 10, paint);
        canvas.drawText(String.valueOf(maxValue), chartX - 5, chartY + 10, paint);

        paint.setTextSize(12);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        canvas.drawText("Days in Reporting Period", chartX + chartWidth / 2, chartY + chartHeight + 35, paint);

        if (!dates.isEmpty()) {
            paint.setTextSize(10);
            paint.setColor(Color.DKGRAY);
            paint.setFakeBoldText(false);

            canvas.drawText(dates.get(0).format(DateTimeFormatter.ofPattern("MM/dd")),
                    chartX, chartY + chartHeight + 15, paint);

            canvas.drawText(dates.get(dates.size() - 1).format(DateTimeFormatter.ofPattern("MM/dd")),
                    chartX + chartWidth, chartY + chartHeight + 15, paint);
        }
    }

    private static class ReportData {
        LocalDate startDate;
        LocalDate endDate;
        String childUid;
        long totalDays;

        int rescueCount = 0;
        double rescuePerWeek = 0;
        Map<LocalDate, Integer> dailyRescueData = new LinkedHashMap<>();

        int controllerDaysTaken = 0;
        double controllerAdherence = 0;

        Map<String, Integer> symptomCounts = new HashMap<>();

        Map<LocalDate, String> dailyZoneData = new LinkedHashMap<>();

        Map<String, Integer> zoneDistribution = new LinkedHashMap<>();

        List<TriageIncident> triageIncidents = new ArrayList<>();

        // 新增：用于跟踪已绘制事件数量的计数器
        int triageIncidentsDrawn = 0;
    }
}