package com.example.demoapp.med;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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

        //TODO Set up buttons
        setupBackButton();

    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }
}
