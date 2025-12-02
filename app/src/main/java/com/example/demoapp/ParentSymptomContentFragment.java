package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ParentSymptomContentFragment extends Fragment {
    private String parentUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_symptom_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button addEntryButton = view.findViewById(R.id.add_entry_button);
        addEntryButton.setOnClickListener(v ->
                {
                    Toast.makeText(getContext(), "add_entry_button clicked", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(requireContext(), AddSymptomsActivity.class);

                    parentUid = getArguments().getString("uid");

                    intent.putExtra("uid", parentUid);
                    intent.putExtra("role", "parent");

                    startActivity(intent);
                }
        );

        if (savedInstanceState == null) {
            SummaryCalendarFragment calendarFragment = new SummaryCalendarFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.display_browse_history_container, calendarFragment)
                    .commit();
        }
    }
}