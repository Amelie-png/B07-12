package com.example.demoapp.med;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demoapp.R;

public class MedicineInventoryActivity extends AppCompatActivity {
    //repo
    private MedicineRepository repo;
    //UI
    private Button btnBack;
    private ImageButton btnEditController;
    private ImageButton btnEditRescue;
    //ID
    private String childId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_inventory);

        // Read intent extras
        childId = getIntent().getStringExtra("childId");

        //Find dynamic items
        btnBack = findViewById(R.id.btn_inventory_back);
        btnEditController = findViewById(R.id.btn_c_edit);
        btnEditRescue = findViewById(R.id.btn_r_edit);

        //Set up repo
        repo = new MedicineRepository();

        //Set up buttons
        setupBackButton();
        setupEditController();
        setupEditRescue();

    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupEditController() {
        btnEditController.setOnClickListener(v -> {
            Intent editController = new Intent(MedicineInventoryActivity.this,
                    ManageControllerActivity.class);
            editController.putExtra("childId", childId);
            editController.putExtra("author", "parent"); //parent is flagging
            startActivity(editController);
        });
    }

    private void setupEditRescue() {
        btnEditRescue.setOnClickListener(v -> {
            Intent editRescue = new Intent(MedicineInventoryActivity.this,
                    ManageRescueActivity.class);
            editRescue.putExtra("childId", childId);
            editRescue.putExtra("author", "parent"); //parent is flagging
            startActivity(editRescue);
        });
    }
}
