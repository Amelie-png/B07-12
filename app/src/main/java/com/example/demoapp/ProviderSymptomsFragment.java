package com.example.demoapp;

import android.os.Bundle;

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
    private Boolean symptomsAllowed, triggersAllowed;

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

        Bundle bundle = getActivity().getIntent().getExtras();
        providerUid = bundle.getString("uid");
        childUid = bundle.getString("childUid");
        symptomsAllowed = bundle.getBoolean("symptomsAllowed");
        triggersAllowed = bundle.getBoolean("triggersAllowed");

        showSymptoms();
    }

    public void showSymptoms(){
        CalendarWithHistory summaryFragment = new CalendarWithHistory();

        Bundle bundle = new Bundle();
        bundle.putString("childUid", childUid);
        bundle.putString("uid", providerUid);
        bundle.putString("role", "provider");
        bundle.putBoolean("symptomsAllowed", symptomsAllowed);
        bundle.putBoolean("triggersAllowed", triggersAllowed);
        summaryFragment.setArguments(bundle);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.summary_calendar_container, summaryFragment)
                .commit();
    }
}
