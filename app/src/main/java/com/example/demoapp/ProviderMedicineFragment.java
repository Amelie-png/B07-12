package com.example.demoapp;

import android.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.demoapp.med.FilterDialogFragment;
import com.example.demoapp.med.FilterState;
import com.example.demoapp.med.ManageRescueActivity;
import com.example.demoapp.med.MedicineAdapter;
import com.example.demoapp.med.MedicineEntry;
import com.example.demoapp.med.MedicineRepository;
import com.example.demoapp.models.Child;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderMedicineFragment extends Fragment {
    //Repo & adapter
    private MedicineRepository repo;
    private MedicineAdapter adapter;
    //Views
    private RecyclerView rv;
    private ImageButton btnRefresh;
    private ImageButton btnFilter;
    private TextView noPermission;
    //Log filters
    private FilterState filterState = new FilterState();
    private boolean rescueVisible;
    private boolean controllerVisible;
    //IDs
    private String childId;
    private String providerUid;


    public ProviderMedicineFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getActivity().getIntent().getExtras();
        if (getArguments() != null) {
            providerUid = bundle.getString("uid");
            childId = bundle.getString("childUid");
        }

        Log.d("ProviderMedicineFragment", "providerUid = " + providerUid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_provider_medicine, container, false);

        //Find dynamic items
        rv = view.findViewById(R.id.rv_med_log);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        btnFilter = view.findViewById(R.id.btn_filter);
        noPermission = view.findViewById(R.id.tv_log_no_permission);

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        hasPermissions();
    }

    private void updateUIAfterPermissionCheck() {
        if (rescueVisible) {
            noPermission.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            applyFiltersAndRefresh();
        } else {
            noPermission.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        }
    }

    private void hasPermissions(){
        repo.fetchShareCode(childId, providerUid, new MedicineRepository.OnResult<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                if (result != null) {

                    Object permissionsObj = result.get("permissions");

                    Map<String, Boolean> permissions =
                            permissionsObj instanceof Map
                                    ? (Map<String, Boolean>) permissionsObj
                                    : new HashMap<>();

                    rescueVisible = Boolean.TRUE.equals(permissions.get("rescueLogs"));
                }


                updateUIAfterPermissionCheck();
            }

            @Override
            public void onFailure(Exception e) {
                // show error dialog
                new AlertDialog.Builder(getContext())
                        .setTitle("Error")
                        .setMessage("Could not fetch share code: " + e.getMessage())
                        .setPositiveButton("OK", null)
                        .show();

                rescueVisible = false;
                updateUIAfterPermissionCheck();
            }
        });
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
                true
        );
        dialog.show(getParentFragmentManager(), "FILTER_DIALOG");
    }

    private void applyFiltersAndRefresh() {
        repo.fetchLogs(
                childId,
                "rescue", //provider can only see rescue logs
                filterState.dateFrom == null ? 0 : filterState.dateFrom,
                filterState.dateTo == null ? Long.MAX_VALUE : filterState.dateTo,
                new MedicineRepository.OnResult<List<MedicineEntry>>() {
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
}
