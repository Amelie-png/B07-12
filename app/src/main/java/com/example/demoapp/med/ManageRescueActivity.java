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
import java.util.Objects;

public class ManageRescueActivity extends AppCompatActivity {
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
    private Button btnSave;
    private Button btnCancel;
    private boolean isUpdating;
    //ID
    private String childId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_rescue);

        // Read intent extras
        childId = getIntent().getStringExtra("childId");

        //Bind dynamic items
        btnBack = findViewById(R.id.btn_r_back_to_inventory);
        btnSave = findViewById(R.id.btn_save_rescue);
        btnCancel = findViewById(R.id.btn_cancel_rescue);

        btnPurchase = findViewById(R.id.btn_purchase_date);
        btnExpiry = findViewById(R.id.btn_expiry_date);

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

        //Set up switch
        setSwitch();
    }

    //Date
    private void setupDate(){
        btnPurchase.setOnClickListener(v -> chooseDate(date -> vm.setPurchaseDate(date)));
        btnExpiry.setOnClickListener(v -> chooseDate(date -> vm.setExpiryDate(date)));

        vm.getPurchaseDate().observe(this, date -> {
            if (date != null)
                btnPurchase.setText(date.toString());
        });

        vm.getExpiryDate().observe(this, date -> {
            if (date != null)
                btnExpiry.setText(date.toString());
        });
    }

    private void chooseDate(ManageControllerActivity.OnDateSelected callback){
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

        editRemainingDoses.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //if (isUpdating) return;
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
                //if (isUpdating) return;
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

//        vm.getRemainingDoses().observe(this, val -> {
//            if (val == null) return;
//
//            //isUpdating = true;
//            editRemainingDoses.setText(String.valueOf(val));
//            //isUpdating = false;
//        });
//
//        vm.getTotalDoses().observe(this, val -> {
//            if (val == null) return;
//
//            //isUpdating = true;
//            editTotalDoses.setText(String.valueOf(val));
//            //isUpdating = false;
//        });
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

    //Save edits
    private void onSave(){
        RescueMed rescue = new RescueMed();
        rescue.setChildId(childId);
        rescue.setPurchaseDate(Objects.requireNonNull(vm.getPurchaseDate().getValue()).toEpochDay());
        rescue.setExpiryDate(Objects.requireNonNull(vm.getExpiryDate().getValue()).toEpochDay());
        rescue.setCurrentAmount(vm.getRemainingDoses().getValue() != null ? vm.getRemainingDoses().getValue() : 0);
        rescue.setTotalAmount(vm.getTotalDoses().getValue() != null ? vm.getTotalDoses().getValue() : 0);
        rescue.setLowStockFlag(vm.getLowStock().getValue() != null ? vm.getLowStock().getValue() : false);
        rescue.setFlagAuthor(vm.getLowStock().getValue() == true ? "parent" : "");

        repo.saveRescueMed(childId, rescue, new MedicineRepository.OnResult<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(ManageRescueActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override public void onFailure(Exception e) {
                // show error dialog
                new AlertDialog.Builder(ManageRescueActivity.this)
                        .setTitle("Error")
                        .setMessage("Could not save rescue settings: " + e.getMessage())
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
