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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class DailyEntryDisplayScreen extends Fragment implements EntryLogRepository.OnEntriesRetrievedListener{

    private ListView listView;
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



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_daily_entries_display_screen, container, false);
        listView = view.findViewById(R.id.entry_list);

        db = FirebaseFirestore.getInstance();
        entryRepo = new EntryLogRepository();
        entryList = new ArrayList<Entry>();

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
            }else if (symptomStr == null) {
                entryList.add(new Entry(Integer.toString(i+1), e.getDate(), e.getRecorder(), triggerStr, false));
            } else if (triggerStr == null){
                entryList.add(new Entry(Integer.toString(i+1), e.getDate(), e.getRecorder(), symptomStr, true));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private String formatSymptomsTriggers(ArrayList<CategoryName> list) {
        if(list==null || list.isEmpty()){
            return "";
        }
        String str = "";
        for(CategoryName s : list){
            str += s.getName() + ", ";
        }
        str = str.substring(0, str.length()-2);
        return str;
    }

    @Override
    public void onError(Exception e) {
        Log.e("DailyEntry", "Error getting entries", e);
    }
}
