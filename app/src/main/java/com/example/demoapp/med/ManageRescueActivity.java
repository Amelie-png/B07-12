package com.example.demoapp.med;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demoapp.R;

public class ManageRescueActivity extends AppCompatActivity {
    //repo
    private MedicineRepository repo;
    //UI
    private Button btnBack;
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

        //Set up repo
        repo = new MedicineRepository();

        //Set up buttons
        setupBackButton();
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }
}
