package com.example.demoapp.med;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.demoapp.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

public class ManageControllerActivity extends AppCompatActivity {
    private MedicineRepository repo;
    private MedicineViewModel vm;

    private Button btnBack, btnSave, btnCancel;
    private Button btnPurchase, btnExpiry, btnStartDate;
    private EditText editRemainingDoses, editTotalDoses, editDailyDose;
    private SwitchMaterial lowFlag;
    private ChipGroup cgDays;

    private String childId;

    private interface OnDateSelected {
        void onSelected(LocalDate date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_controller);

        childId = getIntent().getStringExtra("childId");

        bindViews();
        repo = new MedicineRepository();
        vm = new ViewModelProvider(this).get(MedicineViewModel.class);

        setupButtons();
        setupObservers();
        setupDatePickers();
        setupEditTexts();
        setupSwitch();
        setupChipGroup();

        loadExistingData();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btn_c_back_to_inventory);
        btnSave = findViewById(R.id.btn_save_controller);
        btnCancel = findViewById(R.id.btn_cancel_controller);

        btnPurchase = findViewById(R.id.btn_purchase_date);
        btnExpiry = findViewById(R.id.btn_expiry_date);
        btnStartDate = findViewById(R.id.btn_start_date);

        editRemainingDoses = findViewById(R.id.et_remaining_doses);
        editTotalDoses = findViewById(R.id.et_total_doses);
        editDailyDose = findViewById(R.id.et_dose_per_day);

        lowFlag = findViewById(R.id.switch_low_flag);
        cgDays = findViewById(R.id.cg_schedule_days);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> onCancel());
        btnCancel.setOnClickListener(v -> onCancel());
        btnSave.setOnClickListener(v -> {
            btnSave.setEnabled(false);
            onSave();
        });
    }

    private void setupObservers() {
        vm.getPurchaseDate().observe(this, date ->
                btnPurchase.setText(date != null ? date.toString() : "Select date")
        );
        vm.getExpiryDate().observe(this, date ->
                btnExpiry.setText(date != null ? date.toString() : "Select date")
        );
        vm.getStartDate().observe(this, date ->
                btnStartDate.setText(date != null ? date.toString() : "Select date")
        );
        vm.getLowStock().observe(this, checked -> {
            if (checked != null) lowFlag.setChecked(checked);
        });
        vm.getScheduledDays().observe(this, this::applySelectedDays);
        vm.getRemainingDoses().observe(this, value -> {
            if (value != null) {
                String currentText = editRemainingDoses.getText().toString().trim();
                String newText = String.valueOf(value);
                if (!currentText.equals(newText)) {
                    editRemainingDoses.setText(newText);
                }
            }
        });

        vm.getTotalDoses().observe(this, value -> {
            if (value != null) {
                String currentText = editTotalDoses.getText().toString().trim();
                String newText = String.valueOf(value);
                if (!currentText.equals(newText)) {
                    editTotalDoses.setText(newText);
                }
            }
        });

        vm.getDailyDose().observe(this, value -> {
            if (value != null) {
                String currentText = editDailyDose.getText().toString().trim();
                String newText = String.valueOf(value);
                if (!currentText.equals(newText)) {
                    editDailyDose.setText(newText);
                }
            }
        });
    }

    //Date
    private void setupDatePickers() {
        btnPurchase.setOnClickListener(v -> chooseDate(vm::setPurchaseDate));
        btnExpiry.setOnClickListener(v -> chooseDate(vm::setExpiryDate));
        btnStartDate.setOnClickListener(v -> chooseDate(vm::setStartDate));
    }

    private void chooseDate(OnDateSelected callback) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> callback.onSelected(LocalDate.of(year, month + 1, dayOfMonth)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }
    private void loadExistingData() {
        repo.loadControllerMed(childId, new MedicineRepository.OnResult<ControllerMed>() {
            @Override
            public void onSuccess(ControllerMed med) {
                if (med != null) {
                    // Convert stored Strings to LocalDate
                    vm.setPurchaseDate(med.getPurchaseDate() != null ? LocalDate.parse(med.getPurchaseDate()) : null);
                    vm.setExpiryDate(med.getExpiryDate() != null ? LocalDate.parse(med.getExpiryDate()) : null);
                    vm.setStartDate(med.getStartDate() != null ? LocalDate.parse(med.getStartDate()) : null);

                    vm.setRemainingDoses(med.getCurrentAmount());
                    vm.setTotalDoses(med.getTotalAmount());
                    vm.setDailyDose(med.getDosePerDay());
                    vm.setScheduledDays(med.getScheduleDays());
                    vm.isLowStock(med.isLowStockFlag());
                    vm.setFlagAuthor(med.getFlagAuthor());
                } else {
                    setDefaults();
                }
            }

            @Override
            public void onFailure(Exception e) {
                setDefaults();
            }
        });
    }


    private void setDefaults() {
        vm.setPurchaseDate(null);
        vm.setExpiryDate(null);
        vm.setStartDate(null);
        vm.setRemainingDoses(0);
        vm.setTotalDoses(0);
        vm.setDailyDose(0);
        vm.setScheduledDays(new ArrayList<>());
        vm.isLowStock(false);
        vm.setFlagAuthor("");
    }

    //Edit text
    private void setupEditTexts() {
        editRemainingDoses.addTextChangedListener(new SimpleTextWatcher(value -> vm.setRemainingDoses(value)));
        editTotalDoses.addTextChangedListener(new SimpleTextWatcher(value -> vm.setTotalDoses(value)));
        editDailyDose.addTextChangedListener(new SimpleTextWatcher(value -> vm.setDailyDose(value)));
    }

    //Switch
    private void setupSwitch() {
        lowFlag.setOnCheckedChangeListener((buttonView, isChecked) -> vm.isLowStock(isChecked));
    }

    //Chip group
    private void setupChipGroup() {
        cgDays.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> selected = new ArrayList<>();
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null && chip.getTag() != null) selected.add(chip.getTag().toString());
            }
            vm.setScheduledDays(selected);
        });
    }


    private void applySelectedDays(List<String> days) {
        if (days == null) return;
        cgDays.setOnCheckedStateChangeListener(null);
        for (int i = 0; i < cgDays.getChildCount(); i++) {
            Chip chip = (Chip) cgDays.getChildAt(i);
            if (chip.getTag() != null) chip.setChecked(days.contains(chip.getTag().toString()));
        }
        setupChipGroup(); // reattach listener
    }

    //Save edits
    private void onSave() {
        ControllerMed controller = new ControllerMed();
        controller.setChildId(childId);

        // Save dates as String
        controller.setPurchaseDate(vm.getPurchaseDate().getValue() != null ? vm.getPurchaseDate().getValue().toString() : null);
        controller.setExpiryDate(vm.getExpiryDate().getValue() != null ? vm.getExpiryDate().getValue().toString() : null);
        controller.setStartDate(vm.getStartDate().getValue() != null ? vm.getStartDate().getValue().toString() : null);

        controller.setCurrentAmount(vm.getRemainingDoses().getValue() != null ? vm.getRemainingDoses().getValue() : 0);
        controller.setTotalAmount(vm.getTotalDoses().getValue() != null ? vm.getTotalDoses().getValue() : 0);
        controller.setDosePerDay(vm.getDailyDose().getValue() != null ? vm.getDailyDose().getValue() : 0);
        controller.setScheduleDays(vm.getScheduledDays().getValue());
        controller.setLowStockFlag(vm.getLowStock().getValue() != null ? vm.getLowStock().getValue() : false);
        controller.setFlagAuthor(vm.getLowStock().getValue() != null && vm.getLowStock().getValue() ? "parent" : "");

        repo.saveControllerMed(childId, controller, new MedicineRepository.OnResult<Void>() {
            @Override
            public void onSuccess(Void ignored) {
                Toast.makeText(ManageControllerActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                new AlertDialog.Builder(ManageControllerActivity.this)
                        .setTitle("Error")
                        .setMessage("Could not save: " + e.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void onCancel() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel edit?")
                .setMessage("Are you sure you want to discard the changes? Progress will be lost.")
                .setPositiveButton("Yes", (d, w) -> finish())
                .setNegativeButton("No", null)
                .show();
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Consumer<Integer> callback;

        public SimpleTextWatcher(Consumer<Integer> callback) {
            this.callback = callback;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                callback.accept(Integer.parseInt(s.toString().trim()));
            } catch (Exception e) {
                callback.accept(0);
            }
        }
    }
}
