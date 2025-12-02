package com.example.demoapp.med;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.service.controls.Control;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.demoapp.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.CollectionReference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ManageControllerActivity extends AppCompatActivity {
    //repo
    private MedicineRepository repo;
    //View model
    private MedicineViewModel vm;
    //UI
    private Button btnBack;
    private SwitchMaterial lowFlag;
    private Button btnPurchase;
    private Button btnExpiry;
    private EditText editRemainingDoses;
    private EditText editTotalDoses;
    private Button btnStartDate;
    private EditText editDailyDose;
    private ChipGroup cgDays;
    private Button btnSave;
    private Button btnCancel;
    private boolean isUpdating;
    //ID
    private String childId;

    public interface OnDateSelected {
        void onSelected(LocalDate date);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_controller);

        // Read intent extras
        childId = getIntent().getStringExtra("childId");

        //Bind dynamic items
        btnBack = findViewById(R.id.btn_c_back_to_inventory);
        btnSave = findViewById(R.id.btn_save_controller);
        btnCancel = findViewById(R.id.btn_cancel_controller);

        btnPurchase = findViewById(R.id.btn_purchase_date);
        btnExpiry = findViewById(R.id.btn_expiry_date);
        btnStartDate = findViewById(R.id.btn_start_date);

        //Set up repo
        repo = new MedicineRepository();

        //Set up view model
        vm = new ViewModelProvider(this).get(MedicineViewModel.class);

        //Set up buttons
        btnBack.setOnClickListener(v -> onCancel());
        btnCancel.setOnClickListener(v -> onCancel());
        btnSave.setOnClickListener(v -> {
            btnSave.setEnabled(false);
            onSave();
        });

        //Set up date buttons
        setupDate();

        //Set up edit texts
        setEditText();

        //Set up schedule days
        setDays();

        //Set up switch
        setSwitch();
    }

    //Date
    private void setupDate(){
        btnPurchase.setOnClickListener(v -> chooseDate(date -> vm.setPurchaseDate(date)));
        btnExpiry.setOnClickListener(v -> chooseDate(date -> vm.setExpiryDate(date)));
        btnStartDate.setOnClickListener(v -> chooseDate(date -> vm.setStartDate(date)));

        vm.getPurchaseDate().observe(this, date -> {
            if (date != null)
                btnPurchase.setText(date.toString());
        });

        vm.getExpiryDate().observe(this, date -> {
            if (date != null)
                btnExpiry.setText(date.toString());
        });

        vm.getStartDate().observe(this, date -> {
            if (date != null)
                btnStartDate.setText(date.toString());
        });
    }

    private void chooseDate(OnDateSelected callback){
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    LocalDate date = LocalDate.of(y, m + 1, d);
                    callback.onSelected(date);
                },
                year,
                month,
                day
        );

        dialog.show();
    }

    //Edit text
    private void setEditText(){
        editRemainingDoses = findViewById(R.id.et_remaining_doses);
        editTotalDoses = findViewById(R.id.et_total_doses);
        editDailyDose = findViewById(R.id.et_dose_per_day);

        editRemainingDoses.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                try {
                    vm.setRemainingDoses(Integer.parseInt(s.toString().trim()));
                } catch (Exception e) {
                    // Invalid input → ignore or clear
                    vm.setRemainingDoses(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTotalDoses.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                try {
                    vm.setTotalDoses(Integer.parseInt(s.toString().trim()));
                } catch (Exception e) {
                    // Invalid input → ignore or clear
                    vm.setTotalDoses(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editDailyDose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;
                try {
                    vm.setDailyDose(Integer.parseInt(s.toString().trim()));
                } catch (Exception e) {
                    // Invalid input → ignore or clear
                    vm.setDailyDose(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        vm.getRemainingDoses().observe(this, val -> {
            if (val == null) return;

            isUpdating = true;
            editRemainingDoses.setText(String.valueOf(val));
            isUpdating = false;
        });

        vm.getTotalDoses().observe(this, val -> {
            if (val == null) return;

            isUpdating = true;
            editTotalDoses.setText(String.valueOf(val));
            isUpdating = false;
        });

        vm.getDailyDose().observe(this, val -> {
            if (val == null) return;

            isUpdating = true;
            editDailyDose.setText(String.valueOf(val));
            isUpdating = false;
        });
    }

    //Switch
    private void setSwitch(){
        lowFlag = findViewById(R.id.switch_low_flag);

        lowFlag.setOnCheckedChangeListener((btn, checked) ->{
            vm.isLowStock(checked);
        });

        vm.getLowStock().observe(this, check -> {
            if(check != null)
                lowFlag.setChecked(check);
        });
    }

    //Chip group
    //TODO fix button toggle issue
    private void setDays(){
        cgDays = findViewById(R.id.cg_schedule_days);

        cgDays.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> setDays = new ArrayList<>();

            for(int id : checkedIds){
                if (id == R.id.chip_mon) setDays.add("Mon");
                else if (id == R.id.chip_tue) setDays.add("Tue");
                else if (id == R.id.chip_wed) setDays.add("Wed");
                else if (id == R.id.chip_thu) setDays.add("Thu");
                else if (id == R.id.chip_fri) setDays.add("Fri");
                else if (id == R.id.chip_sat) setDays.add("Sat");
                else if (id == R.id.chip_sun) setDays.add("Sun");
            }

            vm.setScheduledDays(setDays);
        });

        vm.getScheduledDays().observe(this, days -> {
            if (days == null) return;
            for (int i = 0; i < cgDays.getChildCount(); i++) {
                int chipId = cgDays.getChildAt(i).getId();
                boolean checked = days.contains(chipId);
                ((Chip) cgDays.getChildAt(i)).setChecked(checked);
            }
        });
    }

    //Save edits
    private void onSave(){
        ControllerMed controller = new ControllerMed();
        controller.setChildId(childId);
        controller.setPurchaseDate(Objects.requireNonNull(vm.getPurchaseDate().getValue()).toEpochDay());
        controller.setExpiryDate(Objects.requireNonNull(vm.getExpiryDate().getValue()).toEpochDay());
        controller.setStartDate(Objects.requireNonNull(vm.getStartDate().getValue()).toEpochDay());
        controller.setCurrentAmount(vm.getRemainingDoses().getValue() != null ? vm.getRemainingDoses().getValue() : 0);
        controller.setTotalAmount(vm.getTotalDoses().getValue() != null ? vm.getTotalDoses().getValue() : 0);
        controller.setDosePerDay(vm.getDailyDose().getValue() != null ? vm.getDailyDose().getValue() : 0);
        controller.setScheduleDays(vm.getScheduledDays().getValue());
        controller.setLowStockFlag(vm.getLowStock().getValue() != null ? vm.getLowStock().getValue() : false);
        controller.setFlagAuthor(vm.getLowStock().getValue() == true ? "parent" : "");

        repo.saveControllerMed(childId, controller, new MedicineRepository.OnResult<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(ManageControllerActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onFailure(Exception e) {
                // show error dialog
                new AlertDialog.Builder(ManageControllerActivity.this)
                        .setTitle("Error")
                        .setMessage("Could not save controller settings: " + e.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void onCancel() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel edit?")
                .setMessage("Are you sure you want to discard the changes? Progress will be lost.")
                .setPositiveButton("Yes", (d, w) -> {
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
