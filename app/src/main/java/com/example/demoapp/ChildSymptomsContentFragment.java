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

public class ChildSymptomsContentFragment extends Fragment {
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_parent_child_symptom_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            uid = args.getString("uid");
        }

        Button addEntryButton = view.findViewById(R.id.add_entry_button);
        addEntryButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "add_entry_button clicked", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(requireContext(), AddSymptomsActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("role", "child");
            startActivity(intent);
        });

        if (savedInstanceState == null) {
            PlainCalendar calendarFragment = new PlainCalendar();

            Bundle bundle = new Bundle();
            bundle.putString("uid", uid);
            bundle.putString("role", "child");
            calendarFragment.setArguments(bundle);  // FIX: use setArguments()

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.display_browse_history_container, calendarFragment)
                    .commit();
        }
    }
}
