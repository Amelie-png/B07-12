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
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.function.Consumer;

public class ManageRescueActivity extends AppCompatActivity {
    private MedicineRepository repo;
    private MedicineViewModel vm;

    private Button btnBack, btnSave, btnCancel;
    private Button btnPurchase, btnExpiry;
    private EditText editRemainingDoses, editTotalDoses;
    private SwitchMaterial lowFlag;

    private String childId;

    private interface OnDateSelected {
        void onSelected(LocalDate date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_rescue);

        childId = getIntent().getStringExtra("childId");

        bindViews();
        repo = new MedicineRepository();
        vm = new ViewModelProvider(this).get(MedicineViewModel.class);

        setupButtons();
        setupObservers();
        setupDatePickers();
        setupEditTexts();
        setupSwitch();

        loadExistingData();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btn_r_back_to_inventory);
        btnSave = findViewById(R.id.btn_save_rescue);
        btnCancel = findViewById(R.id.btn_cancel_rescue);

        btnPurchase = findViewById(R.id.btn_purchase_date);
        btnExpiry = findViewById(R.id.btn_expiry_date);

        editRemainingDoses = findViewById(R.id.et_remaining_doses);
        editTotalDoses = findViewById(R.id.et_total_doses);

        lowFlag = findViewById(R.id.switch_low_flag);
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
        vm.getLowStock().observe(this, checked -> {
            if (checked != null) lowFlag.setChecked(checked);
        });
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
    }

    //Date
    private void setupDatePickers() {
        btnPurchase.setOnClickListener(v -> chooseDate(vm::setPurchaseDate));
        btnExpiry.setOnClickListener(v -> chooseDate(vm::setExpiryDate));
    }

    private void chooseDate(ManageRescueActivity.OnDateSelected callback) {
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

                    vm.setRemainingDoses(med.getCurrentAmount());
                    vm.setTotalDoses(med.getTotalAmount());
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
        vm.setRemainingDoses(0);
        vm.setTotalDoses(0);
        vm.isLowStock(false);
        vm.setFlagAuthor("");
    }

    //Edit text
    private void setupEditTexts() {
        editRemainingDoses.addTextChangedListener(new ManageRescueActivity.SimpleTextWatcher(value -> vm.setRemainingDoses(value)));
        editTotalDoses.addTextChangedListener(new ManageRescueActivity.SimpleTextWatcher(value -> vm.setTotalDoses(value)));
    }

    //Switch
    private void setupSwitch() {
        lowFlag.setOnCheckedChangeListener((buttonView, isChecked) -> vm.isLowStock(isChecked));
    }

    //Save edits
    private void onSave() {
        RescueMed rescue = new RescueMed();
        rescue.setChildId(childId);

        // Save dates as String
        rescue.setPurchaseDate(vm.getPurchaseDate().getValue() != null ? vm.getPurchaseDate().getValue().toString() : null);
        rescue.setExpiryDate(vm.getExpiryDate().getValue() != null ? vm.getExpiryDate().getValue().toString() : null);

        rescue.setCurrentAmount(vm.getRemainingDoses().getValue() != null ? vm.getRemainingDoses().getValue() : 0);
        rescue.setTotalAmount(vm.getTotalDoses().getValue() != null ? vm.getTotalDoses().getValue() : 0);
        rescue.setLowStockFlag(vm.getLowStock().getValue() != null ? vm.getLowStock().getValue() : false);
        rescue.setFlagAuthor(vm.getLowStock().getValue() != null && vm.getLowStock().getValue() ? "parent" : "");

        repo.saveRescueMed(childId, rescue, new MedicineRepository.OnResult<Void>() {
            @Override
            public void onSuccess(Void ignored) {
                Toast.makeText(ManageRescueActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                new AlertDialog.Builder(ManageRescueActivity.this)
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
