package com.example.demoapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.demoapp.entry_list.Entry;
import com.example.demoapp.pdf_report.PdfReportGenerator;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.util.Calendar;

public class ParentChoosePDFDateRange extends AppCompatActivity {
    private TextInputEditText startDateEditText, endDateEditText;
    private Button btnBack;
    private Button btnExportPdf;
    private String childUid;
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parent_choose_pdf_date_range);

        // -------- 请求存储权限 --------
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            childUid = extras.getString("uid");
        }

        initViews();
        setupDatePickers();
        setupBackButton();
        setupExportPDFButton();
    }

    private void initViews() {
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

    private void setupExportPDFButton() {
        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnExportPdf.setOnClickListener(v -> {
            if (!validateDateRange()) return;

            LocalDate startDate = (LocalDate) startDateEditText.getTag();
            LocalDate endDate = (LocalDate) endDateEditText.getTag();

            if (childUid == null) {
                Toast.makeText(this, "Child UID not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Generating PDF...", Toast.LENGTH_SHORT).show();

            // 调用 PdfReportGenerator
            PdfReportGenerator.generateProviderReport(this, childUid, startDate, endDate);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied. PDF cannot be saved to external storage.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
