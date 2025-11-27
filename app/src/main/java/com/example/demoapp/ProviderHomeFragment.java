package com.example.demoapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.demoapp.card_view.CardAdapter;
import com.example.demoapp.card_view.CardItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProviderHomeFragment extends Fragment {
    private String providerUid;
    private Button addItemButton;
    private RecyclerView recyclerView;
    private ArrayList<CardItem> cardList;
    private CardAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            providerUid = getArguments().getString("uid");
        }

        Log.d("ProviderHomeFragment", "providerUid = " + providerUid);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.provider_home_screen, container, false);


        // Find button
        addItemButton = view.findViewById(R.id.add_item_button);

        if (addItemButton == null) {
            Toast.makeText(getContext(), "Button is NULL!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Button found!", Toast.LENGTH_SHORT).show();
            addItemButton.setOnClickListener(v -> onAddPatientClicked());
        }

        // Find RecyclerView from the inflated view
        recyclerView = view.findViewById(R.id.patient_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data for testing
        cardList = new ArrayList<>();
        cardList.add(new CardItem("A", R.drawable.profile_default_img, R.color.white, "", "", new ArrayList<String>()));
        cardList.add(new CardItem("B", R.drawable.profile_default_img, R.color.white,"", "", new ArrayList<String>()));
        cardList.add(new CardItem("C", R.drawable.profile_default_img, R.color.white, "", "", new ArrayList<String>()));
        cardList.add(new CardItem("D", R.drawable.profile_default_img, R.color.white, "", "", new ArrayList<String>()));

        // Set adapter
        adapter = new CardAdapter(cardList);
        recyclerView.setAdapter(adapter);

        return view;
    }
    private void onAddPatientClicked() {
        Toast.makeText(getContext(), "Button clicked!", Toast.LENGTH_SHORT).show();
        AddPatientPopup popup = new AddPatientPopup();
        popup.show(getParentFragmentManager(), "addPatientPopup");
    }

    private String getProviderID(){
        if (auth.getCurrentUser() != null) {
            String providerId = auth.getCurrentUser().getUid();
            // Now you can use providerId
            Toast.makeText(getContext(), "Current provider UID: " + providerId, Toast.LENGTH_SHORT).show();
            return providerId;
        } else {
            // No user logged in
            Toast.makeText(getContext(), "No provider logged in!", Toast.LENGTH_SHORT).show();
        }
        return null;
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
                if (isAdded()) {
                    new ProviderOnboardingDialog()
                            .show(getChildFragmentManager(), "providerOnboarding");
                }

                // ⭐ 更新 Firestore，防止下次再弹
                doc.update("hasSeenOnboardingProvider", true);
            }
        });
    }


}