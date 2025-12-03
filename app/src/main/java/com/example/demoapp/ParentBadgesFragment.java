package com.example.demoapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.demoapp.med.ManageControllerActivity;
import com.example.demoapp.med.MedicineEntry;
import com.example.demoapp.med.MedicineRepository;
import com.example.demoapp.med.MedicineUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ParentBadgesFragment extends Fragment {

    private String parentUid;
    private String childUid;

    private ImageView badge1Check, badge2Check, badge3Check;
    private Button thresholdBtn;

    private List<MedicineEntry> allEntries = new ArrayList<>();
    private Query medRef;
    private MedicineRepository repo = new MedicineRepository();
    private Badge badge;

    private int rescueThreshold = 4;

    public ParentBadgesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_badges, container, false);

        // Views
        badge1Check = view.findViewById(R.id.badge1_check_image);
        badge2Check = view.findViewById(R.id.badge2_check_image);
        badge3Check = view.findViewById(R.id.badge3_check_image);
        thresholdBtn = view.findViewById(R.id.thresholdButton);

        badge1Check.setVisibility(View.INVISIBLE);
        badge2Check.setVisibility(View.INVISIBLE);
        badge3Check.setVisibility(View.INVISIBLE);

        // Get arguments
        if (getArguments() != null) {
            parentUid = getArguments().getString("parentUid");
            childUid = getArguments().getString("uid");
        }

        // Firestore reference
        medRef = FirebaseFirestore.getInstance()
                .collection("medicine_log")
                .whereEqualTo("childId", childUid);

        loadMedicineEntries();
        setBadge();
        evaluateBadge3();

        return view;
    }

    private void loadMedicineEntries() {
        medRef.get().addOnSuccessListener(snapshot -> {
            allEntries.clear();

            for (QueryDocumentSnapshot doc : snapshot) {
                MedicineEntry entry = doc.toObject(MedicineEntry.class);
                if (entry != null) allEntries.add(entry);
            }

            Log.d("BADGES", "Loaded entries: " + allEntries.size());

            evaluateBadges();
        }).addOnFailureListener(e ->
                Log.e("BADGES", "Error loading entries", e)
        );
    }

    private void evaluateBadges() {
        evaluateBadge3();
    }

    private void setBadge(){
        repo.loadBadges(childUid, new MedicineRepository.OnResult<Badge>() {
            @Override
            public void onSuccess(Badge b) {
                if(b == null) return;
                if(b.getControllerStreak() == 7){
                    badge1Check.setVisibility(View.VISIBLE);
                }
                else {
                    badge1Check.setVisibility(View.INVISIBLE);
                }
                if(b.getTechniqueStreak() == 10){
                    badge2Check.setVisibility(View.VISIBLE);
                }
                else {
                    badge2Check.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    private void evaluateBadge3() {
        int totalRescue = MedicineUtils.getRescueCountByDay(allEntries, 30);

        if (totalRescue <= rescueThreshold) {
            badge3Check.setVisibility(View.VISIBLE);
        }
    }
}
