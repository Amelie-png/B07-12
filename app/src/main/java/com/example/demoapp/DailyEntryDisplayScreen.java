package com.example.demoapp;

import com.example.demoapp.entry_db.CategoryName;
import com.example.demoapp.entry_db.EntryLog;
import com.example.demoapp.entry_db.EntryLogRepository;
import com.example.demoapp.entry_list.*;
import com.google.firebase.firestore.FirebaseFirestore;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class DailyEntryDisplayScreen extends Fragment implements EntryLogRepository.OnEntriesRetrievedListener {

    private ListView listView;
    private TextView whenEmpty;
    private EntryAdapter adapter;
    private ArrayList<Entry> entryList;
    private EntryLogRepository entryRepo;
    private FirebaseFirestore db;
    private String childUid;
    private String role;
    private String startDate;
    private String endDate;
    private ArrayList<String> selectedSymptoms;
    private ArrayList<String> selectedTriggers;
    boolean symptomsAllowed;
    boolean triggersAllowed;

    // ====== 新增：缺失的变量 ======
    private ArrayList<Entry> convertedEntries = new ArrayList<>();
    // ============================

    // ================== 新增接口 ==================
    public interface OnEntriesAvailableListener {
        void onEntriesAvailable(ArrayList<Entry> entries);
    }

    private OnEntriesAvailableListener listener;

    public void setOnEntriesAvailableListener(OnEntriesAvailableListener listener) {
        this.listener = listener;
    }
    // ============================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_daily_entries_display_screen, container, false);
        listView = view.findViewById(R.id.entry_list);
        whenEmpty = view.findViewById(R.id.when_empty);

        db = FirebaseFirestore.getInstance();
        entryRepo = new EntryLogRepository();
        entryList = new ArrayList<>();

        // Get Filter Arguments
        Bundle args = getArguments();
        startDate = args.getString("startDate");
        endDate = args.getString("endDate");

        childUid = args.getString("childId");
        role = args.getString("role");
        selectedSymptoms = args.getStringArrayList("symptoms");
        selectedTriggers = args.getStringArrayList("triggers");
        symptomsAllowed = getArguments().getBoolean("symptomsAllowed");
        triggersAllowed = getArguments().getBoolean("triggersAllowed");

        adapter = new EntryAdapter(getContext(), entryList, symptomsAllowed, triggersAllowed);
        listView.setAdapter(adapter);

        if (startDate != null && endDate != null && childUid != null) {
            loadEntryList();
        }
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadEntryList(){
        entryRepo.getFilteredEntries(this, childUid, startDate, endDate, selectedSymptoms, selectedTriggers);
    }

    @Override
    public void onEntriesRetrieved(ArrayList<EntryLog> entries) {

        entryList.clear();

        int numEntry = entries.size();
        if (numEntry == 0) {
            whenEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            return;
        } else {
            whenEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }

        for (int i=0; i<numEntry; i++) {
            EntryLog e=entries.get(i);
            String symptomStr = null;
            String triggerStr = null;

            if(symptomsAllowed){
                symptomStr = formatSymptomsTriggers(e.getSymptoms());
            }
            if(triggersAllowed){
                triggerStr = formatSymptomsTriggers(e.getTriggers());
            }

            if(symptomStr != null && triggerStr != null){
                entryList.add(new Entry(Integer.toString(i+1), e.getDate(), e.getRecorder(), symptomStr, triggerStr));
            } else if(symptomStr == null && triggerStr == null) {
                entryList.add(new Entry(Integer.toString(i+1), e.getDate(), e.getRecorder()));
            } else if (symptomStr == null) {
                entryList.add(new Entry(Integer.toString(i+1), e.getDate(), e.getRecorder(), triggerStr, false));
            } else if (triggerStr == null){
                entryList.add(new Entry(Integer.toString(i+1), e.getDate(), e.getRecorder(), symptomStr, true));
            }
        }

        adapter.notifyDataSetChanged();

        // 🟢 正确回传 entryList，而不是 convertedEntries！
        if (listener != null) {
            listener.onEntriesAvailable(entryList);
        }
    }


    private String formatSymptomsTriggers(ArrayList<CategoryName> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder str = new StringBuilder();
        for (CategoryName s : list) {
            str.append(s.getName()).append(", ");
        }
        return str.substring(0, str.length() - 2);
    }

    @Override
    public void onError(Exception e) {
        Log.e("DailyEntry", "Error getting entries", e);
    }
}
