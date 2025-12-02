package com.example.demoapp.med;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demoapp.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Override
    protected void onResume() {
        super.onResume();
        loadController();
        loadRescue();
    }

    private void loadController() {
        TextView cPurchaseDate = findViewById(R.id.tv_c_purchase_date);
        TextView cExpiryDate = findViewById(R.id.tv_c_expiry_date);
        TextView cAmount = findViewById(R.id.tv_c_amount);
        TextView dailyDose = findViewById(R.id.tv_c_planned_dose);
        TextView scheduleDays = findViewById(R.id.tv_c_schedule);

        repo.loadControllerMed(childId, new MedicineRepository.OnResult<ControllerMed>() {
            @Override
            public void onSuccess(ControllerMed med) {
                Log.d("DEBUG", "Med loaded: " + med);
                if (med != null) {
                    cPurchaseDate.setText(med.getPurchaseDate() != null ? med.getPurchaseDate() : null);
                    cExpiryDate.setText(med.getExpiryDate() != null ? med.getExpiryDate() : null);

                    cAmount.setText(med.getCurrentAmount() + " / " + med.getTotalAmount() + " Puffs");
                    dailyDose.setText(med.getDosePerDay() + " Doses");

                    List<String> days = med.getScheduleDays();
                    scheduleDays.setText(days != null && !days.isEmpty() ? String.join(", ", days) : "Set");
                } else {
                    cPurchaseDate.setText(null);
                    cExpiryDate.setText(null);

                    cAmount.setText("Set");
                    dailyDose.setText("Set");
                    scheduleDays.setText("Set");
                }
            }

            @Override
            public void onFailure(Exception e) {
                cPurchaseDate.setText(null);
                cExpiryDate.setText(null);

                cAmount.setText("Set");
                dailyDose.setText("Set");
                scheduleDays.setText("Set");
            }
        });
    }

    private void loadRescue() {
        TextView rPurchaseDate = findViewById(R.id.tv_r_purchase_date);
        TextView rExpiryDate = findViewById(R.id.tv_r_expiry_date);
        TextView rAmount = findViewById(R.id.tv_r_amount);

        repo.loadRescueMed(childId, new MedicineRepository.OnResult<RescueMed>() {
            @Override
            public void onSuccess(RescueMed med) {
                Log.d("DEBUG", "Med loaded: " + med);
                if (med != null) {
                    rPurchaseDate.setText(med.getPurchaseDate() != null ? med.getPurchaseDate() : null);
                    rExpiryDate.setText(med.getExpiryDate() != null ? med.getExpiryDate() : null);

                    rAmount.setText(med.getCurrentAmount() + " / " + med.getTotalAmount() + " Puffs");
                } else {
                    rPurchaseDate.setText(null);
                    rExpiryDate.setText(null);

                    rAmount.setText("Set");
                }
            }

            @Override
            public void onFailure(Exception e) {
                rPurchaseDate.setText(null);
                rExpiryDate.setText(null);

                rAmount.setText("Set");
            }
        });
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
