package com.example.demoapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.card_view.CardAdapter;
import com.example.demoapp.card_view.CardItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProviderMain extends AppCompatActivity implements AddPatientPopup.OnDataChangedListener {

    private String providerUid;

    private Button addItemButton;
    private ImageButton profileButton;
    private RecyclerView recyclerView;

    private ArrayList<CardItem> cardList;
    private CardAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_main_screen);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // ----------------------------------------------------
        // GET PROVIDER UID
        // ----------------------------------------------------
        providerUid = getIntent().getStringExtra("providerUid");
        if (providerUid == null) {
            providerUid = getIntent().getStringExtra("uid"); // backup
        }
        if (providerUid == null && auth.getCurrentUser() != null) {
            providerUid = auth.getCurrentUser().getUid();
        }

        if (providerUid == null) {
            Log.e("ProviderMain", "ERROR: Provider UID is NULL");
            finish();
            return;
        }

        // ----------------------------------------------------
        // SET UP UI
        // ----------------------------------------------------
        addItemButton = findViewById(R.id.add_item_button);
        profileButton = findViewById(R.id.provider_profile_button);
        recyclerView = findViewById(R.id.patient_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cardList = new ArrayList<>();

        // Pass providerUid to adapter
        adapter = new CardAdapter(this, cardList, providerUid);
        recyclerView.setAdapter(adapter);

        // BUTTONS
        addItemButton.setOnClickListener(v -> onAddPatientClicked());
        profileButton.setOnClickListener(v -> onProviderProfileClicked());

        // ----------------------------------------------------
        // LOAD ASSIGNED CHILDREN
        // ----------------------------------------------------
        loadChildrenList();
    }

    // ------------------------------------------------------------
    // Add Patient Popup
    // ------------------------------------------------------------
    private void onAddPatientClicked() {
        AddPatientPopup popup = new AddPatientPopup(providerUid);
        popup.setOnDataChangedListener(this);
        popup.show(getSupportFragmentManager(), "addPatientPopup");
    }

    // ------------------------------------------------------------
    // Provider’s Own Profile
    // ------------------------------------------------------------
    private void onProviderProfileClicked() {
        Intent intent = new Intent(ProviderMain.this, ProviderProfileActivity.class);
        intent.putExtra("providerUid", providerUid);
        startActivity(intent);
    }

    // ------------------------------------------------------------
    // Load all children assigned to this provider
    // ------------------------------------------------------------
    private void loadChildrenList() {
        showLoadingDialog();

        db.collection("children")
                .whereArrayContains("providerIds", providerUid)
                .get()
                .addOnSuccessListener(childrenSnapshot -> {

                    ArrayList<CardItem> childrenList = new ArrayList<>();

                    List<DocumentSnapshot> childrenDocs = childrenSnapshot.getDocuments();
                    for (DocumentSnapshot doc : childrenDocs) {

                        String childName = doc.getString("username");
                        String childId = doc.getString("uid");
                        String parentId = doc.getString("parentId");

                        childrenList.add(new CardItem(
                                childName,
                                R.drawable.profile_default_img,
                                R.color.white,
                                childId,
                                parentId,
                                new ArrayList<>()
                        ));
                    }

                    cardList = childrenList;
                    adapter.setList(childrenList);
                    adapter.notifyDataSetChanged();

                    hideLoadingDialog();
                })
                .addOnFailureListener(e -> {
                    Log.e("ProviderMain", "Failed loading children", e);
                    hideLoadingDialog();
                });
    }

    // ------------------------------------------------------------
    // Loading Dialog
    // ------------------------------------------------------------
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            View v = getLayoutInflater().inflate(R.layout.dialog_loading, null);
            builder.setView(v);
            loadingDialog = builder.create();
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    // Called when AddPatientPopup updates data
    @Override
    public void onDataChanged() {
        loadChildrenList();
    }
}
