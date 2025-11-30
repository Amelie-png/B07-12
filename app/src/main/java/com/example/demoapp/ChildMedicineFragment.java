package com.example.demoapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.demoapp.med.FilterDialogFragment;
import com.example.demoapp.med.FilterState;
import com.example.demoapp.med.MedicineAdapter;
import com.example.demoapp.med.MedicineEntry;
import com.example.demoapp.med.MedicineRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ChildMedicineFragment extends Fragment {
    //Repo & adapter
    private MedicineRepository repo;
    private MedicineAdapter adapter;
    //Views
    private RecyclerView rv;
    private FloatingActionButton btnAddLog;
    private ImageButton btnRefresh;
    private ImageButton btnFilter;
    //Log filters
    private FilterState filterState = new FilterState();
    //IDs
    private String childId = "testChildId"; //TODO link actual value
    private String childUid;

    public ChildMedicineFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ---- Retrieve UID from arguments ----
        if (getArguments() != null) {
            childUid = getArguments().getString("uid");
        }

        Log.d("ChildMedicineFragment", "childUid = " + childUid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_child_medicine, container, false);

        //Find dynamic items
        rv = view.findViewById(R.id.rv_med_log);
        btnAddLog = view.findViewById(R.id.fab_add_med_entry);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        btnFilter = view.findViewById(R.id.btn_filter);

        //Set up adapter & repo
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MedicineAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        repo = new MedicineRepository();

        //Set up refresh button
        btnRefresh.setOnClickListener(v -> {
            applyFiltersAndRefresh();
        });

        //Set up filter button
        btnFilter.setOnClickListener(v -> {
            filterDialog();
        });

        //Set up add button
        setupAddButton();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        applyFiltersAndRefresh();   // refresh logs
    }

    //Filter feature
    private void filterDialog() {
        FilterDialogFragment dialog = new FilterDialogFragment(filterState, new FilterDialogFragment.OnFilterApplied() {
            @Override
            public void onApply(FilterState newState) {
                filterState = newState;
                applyFiltersAndRefresh();
                updateFilterIcon();
            }

            @Override
            public void onClear() {
                filterState.clear();
                applyFiltersAndRefresh();
                updateFilterIcon();
            }
        },
                false
        );
        dialog.show(getParentFragmentManager(), "FILTER_DIALOG");
    }

    private void applyFiltersAndRefresh() {
        repo.fetchLogs(
                childId,
                filterState.medType,
                filterState.dateFrom == null ? 0 : filterState.dateFrom,
                filterState.dateTo == null ? Long.MAX_VALUE : filterState.dateTo,
                new MedicineRepository.OnResult<List<MedicineEntry>>(){
                    @Override
                    public void onSuccess(List<MedicineEntry> list) {
                        adapter.updateList(list);
                        Toast.makeText(getContext(), "Refreshed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("LOGS", "Failed to load logs", e);
                    }
                });
    }

    private void updateFilterIcon() {
        if (filterState.hasAnyFilter()) {
            btnFilter.setColorFilter(Color.parseColor("#4CAF50"));  // green
        } else {
            btnFilter.clearColorFilter();
        }
    }

    private void setupAddButton() {
        btnAddLog.setOnClickListener(v -> {
            Intent logWizard = new Intent(getContext(), MedicineLogWizardActivity.class);
            logWizard.putExtra("childId", childId);
            logWizard.putExtra("author", "child"); //child is log author
            startActivity(logWizard);
        });
    }

    //TODO add dialog for edit log
}
