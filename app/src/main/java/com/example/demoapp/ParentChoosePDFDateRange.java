package com.example.demoapp;

import android.app.DatePickerDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.demoapp.entry_list.Entry;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ParentChoosePDFDateRange extends AppCompatActivity {
    private TextInputEditText startDateEditText, endDateEditText;
    private Button btnBack;
    private Button btnExportPdf, btnExportCsv;
    private String childUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            childUid = extras.getString("uid");
        }
        setContentView(R.layout.activity_parent_choose_pdf_date_range);

        initViews();
        setupDatePickers();
        setupBackButton();
        //TODO: Implement other functions
        setupExportPDFButton();
        setupExportCSVButton();
    }

    private void initViews(){
        startDateEditText = findViewById(R.id.startDateEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
    }
    private void setupDatePickers() {
        startDateEditText.setOnClickListener(v -> showDatePicker(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePicker(endDateEditText));
    }

    private void showDatePicker(TextInputEditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    targetEditText.setText((month + 1) + "/" + dayOfMonth + "/" + year);
                    targetEditText.setTag(LocalDate.of(year, month + 1, dayOfMonth));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void setupBackButton() {
        btnBack = findViewById(R.id.btnBackHome);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupExportPDFButton(){
        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnExportPdf.setOnClickListener(v -> {
            if(!validateDateRange()){ return; }
            //TODO: add generate pdf logic
        });
    }

    private void setupExportCSVButton(){
        btnExportCsv = findViewById(R.id.btnExportCsv);
        btnExportCsv.setOnClickListener(v -> {
            if(!validateDateRange()){ return; }
            //TODO: add generate csv logic
        });
    }

    private boolean validateDateRange() {
        LocalDate startDate = (LocalDate) startDateEditText.getTag();
        LocalDate endDate = (LocalDate) endDateEditText.getTag();

        if (startDate == null) {
            Toast.makeText(this, "Please select a start date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (endDate == null) {
            Toast.makeText(this, "Please select an end date", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (startDate.isAfter(endDate)) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            resetDateSelection();
            return false;
        }

        long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate);
        if (monthsBetween < 3 || monthsBetween > 6) {
            Toast.makeText(this, "Date range must be between 3 and 6 months", Toast.LENGTH_SHORT).show();
            resetDateSelection();
            return false;
        }
        return true;
    }

    private void resetDateSelection() {
        startDateEditText.setText("");
        endDateEditText.setText("");
        startDateEditText.setTag(null);
        endDateEditText.setTag(null);
    }
}



/*
public class FilterEntriesScreen extends AppCompatActivity {


    // ======================= PDF / CSV 导出 =======================

    private void setupExportButtons() {
        Button btnExportPdf = findViewById(R.id.btnExportPdf);
        Button btnExportCsv = findViewById(R.id.btnExportCsv);

        btnExportPdf.setOnClickListener(v -> {
            if (lastFilteredEntries == null || lastFilteredEntries.isEmpty()) {
                Toast.makeText(this, "No entries to export.", Toast.LENGTH_SHORT).show();
                return;
            }
            generatePdf(lastFilteredEntries);
        });

        btnExportCsv.setOnClickListener(v -> {
            if (lastFilteredEntries == null || lastFilteredEntries.isEmpty()) {
                Toast.makeText(this, "No entries to export.", Toast.LENGTH_SHORT).show();
                return;
            }
            generateCsv(lastFilteredEntries);
        });
    }

    private void generateCsv(List<Entry> entries) {
        try {
            File path = getExternalFilesDir(null);
            File file = new File(path, "filtered_entries.csv");

            FileWriter writer = new FileWriter(file);

            writer.append("\"Entry Number\",\"Date\",\"Person\",\"Symptoms\",\"Triggers\"\n");

            for (Entry entry : entries) {
                writer.append("\"").append(entry.getEntryNumber()).append("\",");
                writer.append("\"").append(entry.getTimeRecorded()).append("\",");
                writer.append("\"").append(entry.getPerson()).append("\",");

                String symptoms = entry.getSymptoms().replace("\"", "\"\"").replace(",", " | ");
                String triggers = entry.getTriggers().replace("\"", "\"\"").replace(",", " | ");

                writer.append("\"").append(symptoms).append("\",");
                writer.append("\"").append(triggers).append("\"\n");
            }

            writer.flush();
            writer.close();

            Toast.makeText(this, "CSV saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error generating CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void generatePdf(List<Entry> entries) {
        if (entries == null || entries.isEmpty()) {
            Toast.makeText(this, "No entries to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File path = getExternalFilesDir(null);
            File file = new File(path, "filtered_entries.pdf");

            PdfDocument pdf = new PdfDocument();
            Paint titlePaint = new Paint();
            Paint headerPaint = new Paint();
            Paint contentPaint = new Paint();
            Paint bgPaint = new Paint();

            titlePaint.setTextSize(22);
            titlePaint.setFakeBoldText(true);

            headerPaint.setTextSize(16);
            headerPaint.setFakeBoldText(true);

            contentPaint.setTextSize(14);

            int pageWidth = 595;
            int pageHeight = 842;
            int margin = 40;
            int y;
            int lineSpacing = 20;
            boolean alternate = false;

            int pageNumber = 1;
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
            PdfDocument.Page page = pdf.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            y = 60;

            drawTitleAndHeader(canvas, margin, y, titlePaint, headerPaint);
            y += 60;

            for (Entry entry : entries) {
                if (y > pageHeight - margin - 100) {
                    pdf.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                    page = pdf.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 60;
                    drawTitleAndHeader(canvas, margin, y, titlePaint, headerPaint);
                    y += 60;
                }

                if (alternate) {
                    bgPaint.setColor(0xFFEFEFEF);
                    canvas.drawRect(margin - 5, y - 15, pageWidth - margin, y + 100, bgPaint);
                }
                alternate = !alternate;

                canvas.drawText("Entry #: " + safe(entry.getEntryNumber()), margin, y, contentPaint);
                y += lineSpacing;
                canvas.drawText("Date: " + safe(entry.getTimeRecorded()), margin, y, contentPaint);
                y += lineSpacing;
                canvas.drawText("Person: " + safe(entry.getPerson()), margin, y, contentPaint);
                y += lineSpacing;
                canvas.drawText("Symptoms: " + safe(entry.getSymptoms()).replace(",", " | "), margin, y, contentPaint);
                y += lineSpacing;
                canvas.drawText("Triggers: " + safe(entry.getTriggers()).replace(",", " | "), margin, y, contentPaint);
                y += lineSpacing + 10;
            }

            pdf.finishPage(page);

            FileOutputStream fos = new FileOutputStream(file);
            pdf.writeTo(fos);
            pdf.close();

            Toast.makeText(this, "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void drawTitleAndHeader(Canvas canvas, int margin, int y, Paint titlePaint, Paint headerPaint) {
        canvas.drawText("Filtered Daily Entries Report", margin, y, titlePaint);
        y += 40;
        canvas.drawText("Entry Details", margin, y, headerPaint);
    }

    private String safe(String str) {
        return str == null ? "" : str;
    }

}
*/