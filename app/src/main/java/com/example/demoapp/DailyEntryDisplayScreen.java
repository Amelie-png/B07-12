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
    private String startDate;
    private String endDate;
    private ArrayList<String> selectedSymptoms;
    private ArrayList<String> selectedTriggers;


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
        // TODO: replace with actual get childId logic
        childUid = args.getString("childId");

        selectedSymptoms = args.getStringArrayList("symptoms");
        selectedTriggers = args.getStringArrayList("triggers");

        adapter = new EntryAdapter(getContext(), entryList);
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
            String symptomStr = formatSymptomsTriggers(e.getSymptoms());
            String triggerStr = formatSymptomsTriggers(e.getTriggers());
            entryList.add(new Entry(Integer.toString(i+1), e.getDate(), e.getRecorder(), symptomStr, triggerStr));
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
