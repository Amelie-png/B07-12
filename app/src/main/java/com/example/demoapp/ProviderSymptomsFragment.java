package com.example.demoapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;

public class ProviderSymptomsFragment extends Fragment {

    private String providerUid;
    private String childUid;

    public ProviderSymptomsFragment() {}

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provider_symptoms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        providerUid = getActivity().getIntent().getExtras().getString("uid");
        childUid = getActivity().getIntent().getExtras().getString("childUid");

        showSymptoms();
    }

    public void showSymptoms(){
        SummaryCalendarFragment summaryFragment = new SummaryCalendarFragment();

        Bundle bundle = new Bundle();
        bundle.putString("childUid", childUid);
        bundle.putString("uid", providerUid);
        bundle.putString("role", "provider");
        summaryFragment.setArguments(bundle);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.summary_calendar_container, summaryFragment)
                .commit();
    }
}
