package com.example.demoapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.demoapp.card_view.CardAdapter;
import com.example.demoapp.card_view.CardItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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

        // providerUid passed from login or previous Activity
        providerUid = getIntent().getStringExtra("uid");
        //TODO: replace with args
        if (providerUid == null) {
            providerUid = "65eaII6T0dTv20cnH6ZNPGkkHTQ2"; // remove later
        }

        // find buttons
        addItemButton = findViewById(R.id.add_item_button);
        profileButton = findViewById(R.id.provider_profile_button);
        recyclerView = findViewById(R.id.patient_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Card list setup
        cardList = new ArrayList<>();
        adapter = new CardAdapter(this, cardList);
        recyclerView.setAdapter(adapter);

        addItemButton.setOnClickListener(v -> onAddPatientClicked());
        profileButton.setOnClickListener(v -> onProfileButtonClicked());

        // Load data
        loadChildrenList();
    }

    private void onAddPatientClicked() {
        AddPatientPopup popup = new AddPatientPopup(providerUid);
        popup.setOnDataChangedListener(this);
        popup.show(getSupportFragmentManager(), "addPatientPopup");
    }

    private void onProfileButtonClicked(){
        Intent intent = new Intent(ProviderMain.this, ProviderProfileActivity.class);
        startActivity(intent);
    }

    private void loadChildrenList(){
        showLoadingDialog();
        ArrayList<CardItem> childrenList = new ArrayList<CardItem>();
        db.collection("children")
                .whereArrayContains("providerIds", providerUid)
                .get()
                .addOnSuccessListener(childrenSnapshot -> {
                    List<DocumentSnapshot> childrenDocs = childrenSnapshot.getDocuments();
                    for (DocumentSnapshot individualChildDoc : childrenDocs) {
                        String childName = individualChildDoc.getString("username");
                        String childId = individualChildDoc.getString("uid");
                        String parentId = individualChildDoc.getString("parentId");
                        childrenList.add(new CardItem(childName, R.drawable.profile_default_img, R.color.white, childId, parentId, new ArrayList<String>()));
                    }
                    cardList = childrenList;
                    adapter.setList(childrenList);
                    adapter.notifyDataSetChanged();
                    hideLoadingDialog();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreCheck", "Error fetching children collection", e);
                    hideLoadingDialog();
                });
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);

            View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
            builder.setView(view);

            loadingDialog = builder.create();
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onDataChanged() {
        loadChildrenList();
    }

    // ------------------------------------------------------------
    //  onboarding popup
    // ------------------------------------------------------------
    private void checkProviderOnboarding() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid == null) {
            return;  // Provider 未登录
        }

        DocumentReference doc = db.collection("users").document(uid);

        doc.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) return;

            Boolean hasSeen = snapshot.getBoolean("hasSeenOnboardingProvider");

            if (hasSeen == null || !hasSeen) {

                // ⭐ 显示 Provider Onboarding
                //TODO: Double check with ChenXin
                //if (isAdded()) {
                    new ProviderOnboardingDialog()
                            .show(getSupportFragmentManager(), "providerOnboarding");
                //}

                // ⭐ 更新 Firestore，防止下次再弹
                doc.update("hasSeenOnboardingProvider", true);
            }
        });
    }


}