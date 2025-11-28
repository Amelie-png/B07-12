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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProviderHomeFragment extends Fragment implements AddPatientPopup.OnDataChangedListener {
    private String providerUid;
    private Button addItemButton;
    private RecyclerView recyclerView;
    private ArrayList<CardItem> cardList;
    private CardAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            providerUid = getArguments().getString("uid");
        }
        //TODO: delete else
        else{
            providerUid = "65eaII6T0dTv20cnH6ZNPGkkHTQ2";
        }

        Log.d("ProviderHomeFragment", "providerUid = " + providerUid);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.provider_home_screen, container, false);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

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

        // Load Children card view
        cardList = new ArrayList<>();
        adapter = new CardAdapter(cardList);
        recyclerView.setAdapter(adapter);

        loadChildrenList();

        return view;
    }
    private void onAddPatientClicked() {
        AddPatientPopup popup = new AddPatientPopup(providerUid);
        popup.setOnDataChangedListener(this);
        popup.show(getParentFragmentManager(), "addPatientPopup");
    }

    private void loadChildrenList(){
        ArrayList<CardItem> childrenList = new ArrayList<CardItem>();
        db.collection("children")
                .get()
                .addOnSuccessListener(childrenSnapshot -> {
                    List<DocumentSnapshot> childrenDocs = childrenSnapshot.getDocuments();
                    for (DocumentSnapshot individualChildDoc : childrenDocs) {
                        // Check if providerIds  contains the current providerId
                        ArrayList<String> providerIds = (ArrayList<String>) individualChildDoc.get("providerIds");
                        if (providerIds != null && providerIds.contains(providerUid)) {
                            String childName = individualChildDoc.getString("username");
                            String childId = individualChildDoc.getString("uid");
                            String parentId = individualChildDoc.getString("parentId");
                            childrenList.add(new CardItem(childName, R.drawable.profile_default_img, R.color.white, childId, parentId, new ArrayList<String>()));
                        }
                    }
                    cardList = childrenList;
                    adapter.setList(childrenList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("FirestoreCheck", "Error fetching children collection", e));
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