package com.example.demoapp;

import com.example.demoapp.card_view.*;
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

import java.util.ArrayList;

public class ProviderHomeScreen extends Fragment {
    private Button addItemButton;
    private RecyclerView recyclerView;
    private ArrayList<CardItem> cardList;
    private CardAdapter adapter;

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
}