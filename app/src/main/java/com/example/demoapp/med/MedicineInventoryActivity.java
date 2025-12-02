package com.example.demoapp.med;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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

        //Set up views
        loadController();
        loadRescue();

    }

    private void loadController(){
        TextView cPurchaseDate = findViewById(R.id.tv_c_purchase_date);
        TextView cExpiryDate = findViewById(R.id.tv_c_expiry_date);
        TextView cAmount = findViewById(R.id.tv_c_amount);
        TextView dailyDose = findViewById(R.id.tv_c_planned_dose);
        TextView scheduleDays = findViewById(R.id.tv_c_schedule);

        repo.loadControllerMed(childId, new MedicineRepository.OnResult<>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                if (result == null) return;

                // Dates
                Object purchase = result.get("purchaseDate");
                Object expiry = result.get("expiryDate");

                cPurchaseDate.setText(purchase != null ? purchase.toString() : "Set date");
                cExpiryDate.setText(expiry != null ? expiry.toString() : "Set date");

                // Numbers
                Object daily = result.get("dosePerDay");
                Object remaining = result.get("currentAmount");
                Object total = result.get("totalAmount");

                dailyDose.setText(daily != null ? daily + " Doses" : "Set");
                cAmount.setText(remaining != null && total != null ? remaining + " / " + total + " Puffs" : "Set");

                //Chip group
                Object daysObj = result.get("scheduleDays");
                if (daysObj instanceof List) {
                    List<String> days = new ArrayList<>();
                    for (Object o : (List<?>) daysObj) {
                        if (o instanceof String) days.add((String) o);
                    }
                    scheduleDays.setText(String.join(", ", days));
                }
            }

            @Override
            public void onFailure(Exception e) {
                // show error dialog
                new AlertDialog.Builder(MedicineInventoryActivity.this)
                        .setTitle("Error")
                        .setMessage("Could not load existing settings: " + e.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void loadRescue(){
        TextView purchaseDate = findViewById(R.id.tv_r_purchase_date);
        TextView expiryDate = findViewById(R.id.tv_r_expiry_date);
        TextView amount = findViewById(R.id.tv_r_amount);

        repo.loadRescueMed(childId, new MedicineRepository.OnResult<>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                if (result == null) return;

                // Dates
                Object purchase = result.get("purchaseDate");
                Object expiry = result.get("expiryDate");

                purchaseDate.setText(purchase != null ? purchase.toString() : "Set date");
                expiryDate.setText(expiry != null ? expiry.toString() : "Set date");

                // Numbers
                Object remaining = result.get("currentAmount");
                Object total = result.get("totalAmount");

                amount.setText(remaining != null && total != null ? remaining + " / " + total + " Puffs" : "Set");

            }

            @Override
            public void onFailure(Exception e) {
                // show error dialog
                new AlertDialog.Builder(MedicineInventoryActivity.this)
                        .setTitle("Error")
                        .setMessage("Could not load existing settings: " + e.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
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
