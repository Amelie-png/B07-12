package com.example.demoapp.pdf_report;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import com.example.demoapp.entry_db.CategoryName;
import com.example.demoapp.entry_db.EntryLog;
import com.example.demoapp.entry_db.EntryLogRepository;
import com.example.demoapp.med.ControllerMed;
import com.example.demoapp.med.MedicineEntry;
import com.example.demoapp.med.MedicineUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfReportGenerator {

    public static void generateProviderReport(Context context,
                                              String childId,
                                              LocalDate start,
                                              LocalDate end) {

        PdfDocument document = new PdfDocument();

        // -----------------------------
        // 1. 获取 EntryLog 数据
        // -----------------------------
        EntryLogRepository repo = new EntryLogRepository();
        repo.getAllEntries(new EntryLogRepository.OnEntriesRetrievedListener() {
            @Override
            public void onEntriesRetrieved(ArrayList<EntryLog> entries) {

                // 过滤 childId 和日期范围
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                List<EntryLog> filteredEntries = new ArrayList<>();
                for (EntryLog e : entries) {
                    if (!childId.equals(e.getChildUid())) continue;
                    LocalDate entryDate = LocalDate.parse(e.getDate(), formatter);
                    if (!entryDate.isBefore(start) && !entryDate.isAfter(end)) {
                        filteredEntries.add(e);
                    }
                }

                // -----------------------------
                // 2. 获取 Medicine Entries 列表
                // -----------------------------
                List<MedicineEntry> medicineEntries = new ArrayList<>();
                for (EntryLog e : filteredEntries) {
                    try {
                        // 假设 EntryLog 里有 medicineEntries 字段 public List<MedicineEntry> medicineEntries
                        if (e.medicineEntries != null) {
                            medicineEntries.addAll(e.medicineEntries);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                // -----------------------------
                // 3. 获取 Controller 配置
                // -----------------------------
                FirebaseFirestore.getInstance().collection("children").document(childId)
                        .get().addOnSuccessListener(doc -> {
                            ControllerMed controllerMed = doc.toObject(ControllerMed.class);

                            // -----------------------------
                            // 4. Zone Distribution
                            // -----------------------------
                            Map<String,Integer> zoneCounts = new HashMap<>();
                            zoneCounts.put("GREEN",0); zoneCounts.put("YELLOW",0); zoneCounts.put("RED",0);
                            for (EntryLog e : filteredEntries) {
                                try {
                                    // 假设 EntryLog 有 field triageState
                                    String z = e.triageState != null ? e.triageState : "GREEN";
                                    zoneCounts.put(z, zoneCounts.getOrDefault(z,0)+1);
                                } catch (Exception ex) { ex.printStackTrace(); }
                            }

                            // -----------------------------
                            // 5. Symptom Counts
                            // -----------------------------
                            ArrayList<String> symptomCategories = new ArrayList<>();
                            symptomCategories.add("Cough");
                            symptomCategories.add("Wheeze");
                            symptomCategories.add("Breathlessness");
                            symptomCategories.add("Other");

                            repo.countProblemDaysByCategories(childId, start, end, symptomCategories,
                                    new EntryLogRepository.OnProblemDaysCountedByCategoryListener() {
                                        @Override
                                        public void onCounted(Map<String, Integer> symptomCounts) {

                                            // -----------------------------
                                            // 6. Controller Adherence & Rescue Frequency
                                            // -----------------------------
                                            long startMillis = start.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                                            long endMillis = end.atTime(23,59).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

                                            int totalDays = (int)((endMillis - startMillis)/(24*60*60*1000L)) +1;
                                            double adherence = MedicineUtils.calculateControllerAdherence(medicineEntries, controllerMed, startMillis, endMillis);

                                            List<Integer> rescueCounts = new ArrayList<>();
                                            for (int i = 0; i < totalDays; i++) {
                                                long dayStart = startMillis + i*24*60*60*1000L;
                                                long dayEnd = dayStart + 24*60*60*1000L -1;
                                                int dailyRescue = MedicineUtils.countRescueUsage(medicineEntries, dayStart, dayEnd);
                                                rescueCounts.add(dailyRescue);
                                            }

                                            // -----------------------------
                                            // 7. 生成 PDF
                                            // -----------------------------
                                            try {
                                                File file = new File(Environment.getExternalStorageDirectory(), "provider_report.pdf");

                                                // PAGE 1: Rescue Frequency (折线图)
                                                PdfDocument.PageInfo pageInfo1 = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                                                PdfDocument.Page page1 = document.startPage(pageInfo1);
                                                Canvas canvas1 = page1.getCanvas();
                                                drawLineChart(canvas1, rescueCounts, "Rescue Frequency", "Number of rescue uses per day");
                                                document.finishPage(page1);

                                                // PAGE 2: Controller Adherence (柱状图)
                                                Map<String, Integer> adherenceMap = new HashMap<>();
                                                adherenceMap.put("Adherence %", (int)(adherence*100));
                                                PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(595, 842, 2).create();
                                                PdfDocument.Page page2 = document.startPage(pageInfo2);
                                                Canvas canvas2 = page2.getCanvas();
                                                drawBarChart(canvas2, adherenceMap, "Controller Adherence", "Percentage of planned doses completed");
                                                document.finishPage(page2);

                                                // PAGE 3: Zone Distribution (饼图)
                                                PdfDocument.PageInfo pageInfo3 = new PdfDocument.PageInfo.Builder(595, 842, 3).create();
                                                PdfDocument.Page page3 = document.startPage(pageInfo3);
                                                Canvas canvas3 = page3.getCanvas();
                                                drawPieChart(canvas3, zoneCounts, "Zone Distribution", "Number of days in each triage zone");
                                                document.finishPage(page3);

                                                // PAGE 4: Symptom Burden (柱状图)
                                                PdfDocument.PageInfo pageInfo4 = new PdfDocument.PageInfo.Builder(595, 842, 4).create();
                                                PdfDocument.Page page4 = document.startPage(pageInfo4);
                                                Canvas canvas4 = page4.getCanvas();
                                                drawBarChart(canvas4, symptomCounts, "Symptom Burden", "Counts of problem days by symptom");
                                                document.finishPage(page4);

                                                // 保存 PDF
                                                FileOutputStream fos = new FileOutputStream(file);
                                                document.writeTo(fos);
                                                document.close();
                                                fos.close();
                                                Toast.makeText(context, "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                        @Override
                                        public void onError(Exception e) { e.printStackTrace(); }
                                    });

                        }).addOnFailureListener(Throwable::printStackTrace);

            }

            @Override
            public void onError(Exception e) { e.printStackTrace(); }
        });

    }

    // -----------------------------
    // 绘图函数（统一标题+描述）
    // -----------------------------
    private static void drawBarChart(Canvas canvas, Map<String, Integer> counts, String title, String description) {
        int pageWidth = 595, pageHeight = 842;
        int margin = 50;
        int chartHeight = 300;
        int chartWidth = pageWidth - 2 * margin;
        int chartTop = 150;
        int chartBottom = chartTop + chartHeight;

        Paint paint = new Paint();
        paint.setColor(0xFF4285F4); // 柱子颜色
        paint.setStyle(Paint.Style.FILL);

        Paint axisPaint = new Paint();
        axisPaint.setColor(0xFF000000);
        axisPaint.setStrokeWidth(3);

        Paint textPaint = new Paint();
        textPaint.setColor(0xFF000000);

        // 标题
        textPaint.setTextSize(36);
        canvas.drawText(title, margin, 50, textPaint);

        // 副标题
        textPaint.setTextSize(24);
        canvas.drawText(description, margin, 90, textPaint);

        // 坐标轴
        canvas.drawLine(margin, chartBottom, margin + chartWidth, chartBottom, axisPaint); // x轴
        canvas.drawLine(margin, chartTop, margin, chartBottom, axisPaint); // y轴

        String[] keys = counts.keySet().toArray(new String[0]);
        int n = keys.length;
        int spacing = chartWidth / (n + 1);
        int max = counts.values().stream().max(Integer::compareTo).orElse(1);

        textPaint.setTextSize(20);

        for (int i = 0; i < n; i++) {
            int x = margin + (i + 1) * spacing;
            int barHeight = (int) ((counts.get(keys[i]) / (float) max) * chartHeight);
            int yTop = chartBottom - barHeight;

            // 绘制柱子
            canvas.drawRect(x - 20, yTop, x + 20, chartBottom, paint);

            // x轴标签
            float labelWidth = textPaint.measureText(keys[i]);
            canvas.drawText(keys[i], x - labelWidth / 2, chartBottom + 25, textPaint);

            // 数值标签
            String valStr = String.valueOf(counts.get(keys[i]));
            float valWidth = textPaint.measureText(valStr);
            canvas.drawText(valStr, x - valWidth / 2, yTop - 10, textPaint);
        }
    }

    private static void drawLineChart(Canvas canvas, List<Integer> data, String title, String description) {
        int pageWidth = 595, pageHeight = 842;
        int margin = 50;
        int chartHeight = 300;
        int chartWidth = pageWidth - 2 * margin;
        int chartTop = 150;
        int chartBottom = chartTop + chartHeight;

        Paint linePaint = new Paint();
        linePaint.setColor(0xFF4285F4);
        linePaint.setStrokeWidth(4);
        linePaint.setStyle(Paint.Style.STROKE);

        Paint pointPaint = new Paint();
        pointPaint.setColor(0xFFEA4335);
        pointPaint.setStrokeWidth(8);

        Paint axisPaint = new Paint();
        axisPaint.setColor(0xFF000000);
        axisPaint.setStrokeWidth(3);

        Paint textPaint = new Paint();
        textPaint.setColor(0xFF000000);

        // 标题
        textPaint.setTextSize(36);
        canvas.drawText(title, margin, 50, textPaint);
        textPaint.setTextSize(24);
        canvas.drawText(description, margin, 90, textPaint);

        // 坐标轴
        canvas.drawLine(margin, chartBottom, margin + chartWidth, chartBottom, axisPaint); // x轴
        canvas.drawLine(margin, chartTop, margin, chartBottom, axisPaint); // y轴

        int n = data.size();
        int max = data.stream().max(Integer::compareTo).orElse(1);
        float xStep = chartWidth / (float) (n - 1);

        // 绘制折线
        for (int i = 0; i < n - 1; i++) {
            float x1 = margin + i * xStep;
            float y1 = chartBottom - (data.get(i) / (float) max) * chartHeight;
            float x2 = margin + (i + 1) * xStep;
            float y2 = chartBottom - (data.get(i + 1) / (float) max) * chartHeight;
            canvas.drawLine(x1, y1, x2, y2, linePaint);
            canvas.drawCircle(x1, y1, 6, pointPaint);
        }
        // 最后一个点
        float lastX = margin + (n - 1) * xStep;
        float lastY = chartBottom - (data.get(n - 1) / (float) max) * chartHeight;
        canvas.drawCircle(lastX, lastY, 6, pointPaint);
    }

    private static void drawPieChart(Canvas canvas, Map<String, Integer> counts, String title, String description) {
        int pageWidth = 595, pageHeight = 842;
        int margin = 50;
        int centerX = pageWidth / 2;
        int centerY = 450;
        int radius = 150;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        Paint textPaint = new Paint();
        textPaint.setColor(0xFF000000);
        textPaint.setTextSize(30);

        canvas.drawText(title, margin, 50, textPaint);
        canvas.drawText(description, margin, 90, textPaint);

        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        int startAngle = 0;
        int[] colors = {0xFF4285F4, 0xFF34A853, 0xFFFFC107, 0xFFEA4335};

        String[] keys = counts.keySet().toArray(new String[0]);
        for (int i = 0; i < keys.length; i++) {
            int sweep = (int) (360f * counts.get(keys[i]) / total);
            paint.setColor(colors[i % colors.length]);
            canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, startAngle, sweep, true, paint);
            startAngle += sweep;
        }

        // 图例
        int legendX = margin;
        int legendY = centerY + radius + 50;
        int legendBox = 30;
        for (int i = 0; i < keys.length; i++) {
            paint.setColor(colors[i % colors.length]);
            canvas.drawRect(legendX, legendY + i * 40, legendX + legendBox, legendY + i * 40 + legendBox, paint);
            canvas.drawText(keys[i] + " (" + counts.get(keys[i]) + ")", legendX + legendBox + 10, legendY + 20 + i * 40, textPaint);
        }
    }


    // -----------------------------
    // Firebase 异步接口
    // -----------------------------
    private interface MedicineEntriesCallback { void onLoaded(List<MedicineEntry> entries); void onError(Exception e); }
    private interface ControllerConfigCallback { void onLoaded(ControllerMed config); void onError(Exception e); }
    private interface ZoneDistributionCallback { void onLoaded(Map<String,Integer> counts); void onError(Exception e); }
    private interface ProblemDaysCallback { void onLoaded(Map<String,Integer> counts); void onError(Exception e); }

    // -----------------------------
    // Firebase 数据读取函数
    // -----------------------------
    private static void loadAllMedicineEntries(String childId, LocalDate start, LocalDate end,
                                               MedicineEntriesCallback callback) {
        EntryLogRepository repo = new EntryLogRepository();
        repo.getAllEntries(new EntryLogRepository.OnEntriesRetrievedListener() {
            @Override
            public void onEntriesRetrieved(ArrayList<EntryLog> entries) {
                List<MedicineEntry> meds = new ArrayList<>();
                for (EntryLog e : entries) {
                    // 筛选 childId
                    if (!childId.equals(e.getChildUid())) continue;

                    // 筛选日期范围
                    LocalDate entryDate = LocalDate.parse(e.getDate());
                    if (entryDate.isBefore(start) || entryDate.isAfter(end)) continue;

                    if (e.getMedicineEntries() != null) meds.addAll(e.getMedicineEntries());
                }
                callback.onLoaded(meds);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }


    private static void loadControllerMedConfig(String childId, ControllerConfigCallback callback) {
        FirebaseFirestore.getInstance()
                .collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(doc -> {
                    Map<String,Object> map = doc.getData(); // 获取原始 Map
                    ControllerMed config = ControllerMed.fromMap(map); // 使用 fromMap 反序列化
                    if (config != null) callback.onLoaded(config);
                    else callback.onError(new Exception("No controller config"));
                })
                .addOnFailureListener(callback::onError);
    }

    private static void loadZoneDistribution(String childId, LocalDate start, LocalDate end,
                                             ZoneDistributionCallback callback) {
        EntryLogRepository repo = new EntryLogRepository();
        repo.getAllEntries(new EntryLogRepository.OnEntriesRetrievedListener() {
            @Override
            public void onEntriesRetrieved(ArrayList<EntryLog> entries) {
                Map<String, Integer> zoneCounts = new HashMap<>();
                zoneCounts.put("GREEN", 0);
                zoneCounts.put("YELLOW", 0);
                zoneCounts.put("RED", 0);

                for (EntryLog e : entries) {
                    // 筛选 childId
                    if (!childId.equals(e.getChildUid())) continue;

                    // 筛选日期范围
                    LocalDate entryDate = LocalDate.parse(e.getDate());
                    if (entryDate.isBefore(start) || entryDate.isAfter(end)) continue;

                    // 获取 triageState
                    String z = e.getTriageState();  // 确保 EntryLog.java 中有 public String getTriageState()
                    if (z == null) z = "GREEN"; // 默认值
                    zoneCounts.put(z, zoneCounts.getOrDefault(z, 0) + 1);
                }
                callback.onLoaded(zoneCounts);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }


    private static void loadProblemDaysCounts(String childId, LocalDate start, LocalDate end,
                                              ArrayList<String> categories, ProblemDaysCallback callback) {
        EntryLogRepository repo = new EntryLogRepository();
        repo.countProblemDaysByCategories(childId, start, end, categories,
                new EntryLogRepository.OnProblemDaysCountedByCategoryListener() {
                    @Override
                    public void onCounted(Map<String, Integer> counts) {
                        callback.onLoaded(counts);
                    }
                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
    }
}
