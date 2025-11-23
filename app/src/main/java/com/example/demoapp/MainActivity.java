package com.example.demoapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import com.example.demoapp.card_view.CardAdapter;
import com.example.demoapp.card_view.CardItem;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private ArrayList<CardItem> cardList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // your layout with FrameLayout

        // Only add fragment if this is the first creation
        if (savedInstanceState == null) {
            //testing summary calendar
            /*SummaryCalendarFragment calendarFragment = new SummaryCalendarFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, calendarFragment);
            fragmentTransaction.commit();*/
            //testing display calendar entries
            //DailyEntryDisplayScreen calendarFragment = new DailyEntryDisplayScreen();
            //testing filter screen
            FilterEntriesScreen filterFragment = new FilterEntriesScreen();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_container, filterFragment);
            fragmentTransaction.commit();
            //}
        }

        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProviderHomeScreen())
                    .commit();
        }*/
    }
}

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }*/
