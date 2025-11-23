package com.example.demoapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class BadgesFragment extends Fragment {

    public BadgesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_badges, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ---------- BADGE COLLECTION ----------
        RecyclerView badgeRecycler = view.findViewById(R.id.recyclerBadges);
        badgeRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        List<Badge> badges = new ArrayList<>();
        badges.add(new Badge(R.drawable.ic_badge));
        badges.add(new Badge(R.drawable.ic_badge));
        badges.add(new Badge(R.drawable.ic_badge));
        badges.add(new Badge(R.drawable.ic_badge));

        BadgeAdapter badgeAdapter = new BadgeAdapter(badges);
        badgeRecycler.setAdapter(badgeAdapter);


        // ---------- TROPHY COLLECTION ----------
        RecyclerView trophyRecycler = view.findViewById(R.id.recyclerTrophies);
        trophyRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        List<Trophy> trophies = new ArrayList<>();
        trophies.add(new Trophy(R.drawable.ic_trophy, "October"));
        trophies.add(new Trophy(R.drawable.ic_trophy, "September"));
        trophies.add(new Trophy(R.drawable.ic_trophy, "August"));

        TrophyAdapter trophyAdapter = new TrophyAdapter(trophies);
        trophyRecycler.setAdapter(trophyAdapter);
    }
}
