package com.example.demoapp.card_view;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.MainNavActivity;
import com.example.demoapp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView profilePic;
        ImageView profileBackground;
        CardView cardRoot;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.card_root);
            name = itemView.findViewById(R.id.card_view_patient_name);
            profilePic = itemView.findViewById(R.id.card_view_patient_profile_pic);
            profileBackground = itemView.findViewById(R.id.cardview_patient_background);
        }
    }

    private ArrayList<CardItem> list;
    private Activity activity;
    private String providerUid;

    // Constructor
    public CardAdapter(Activity activity, ArrayList<CardItem> list, String providerUid) {
        this.activity = activity;
        this.list = list;
        this.providerUid = providerUid;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_item_patient, parent, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardItem item = list.get(position);

        holder.name.setText(item.name);
        holder.profilePic.setImageResource(item.profilePic);
        holder.profileBackground.setImageResource(item.profileBackground);

        holder.cardRoot.setOnClickListener(v -> {
            String childUid = item.childId;

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("children").document(childUid).get()
                    .addOnSuccessListener(childDoc -> {
                        if (!childDoc.exists()) {
                            Toast.makeText(activity, "Child not found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //get provider bindings map
                        Map<String, Object> providerBindings =
                                (Map<String, Object>) childDoc.get("providerBindings");

                        if (providerBindings == null || !providerBindings.containsKey(providerUid)) {
                            Toast.makeText(activity, "No binding found for provider", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //get share code DocRef
                        String shareCode = providerBindings.get(providerUid).toString();
                        Map<String, Object> shareCodes = (Map<String, Object>) childDoc.get("shareCodes");

                        if (shareCodes == null || !shareCodes.containsKey(shareCode)) {
                            Toast.makeText(activity, "Share code not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Object> codeEntry =
                                (Map<String, Object>) shareCodes.get(shareCode);

                        Map<String, Object> permissions =
                                (Map<String, Object>) codeEntry.get("permissions");

                        Boolean symptomsAllowed = (Boolean) permissions.get("symptoms");
                        Boolean triggersAllowed = (Boolean) permissions.get("triggers");

                        //start main nav activity
                        Intent intent = new Intent(activity, MainNavActivity.class);
                        intent.putExtra("childUid", childUid);
                        intent.putExtra("shareCode", shareCode);
                        intent.putExtra("symptomsAllowed", symptomsAllowed != null && symptomsAllowed);
                        intent.putExtra("triggersAllowed", triggersAllowed != null && triggersAllowed);
                        intent.putExtra("role", "provider");
                        intent.putExtra("uid", activity.getIntent().getExtras().getString("uid"));
                        intent.putExtra("role", "provider");

                        activity.startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(activity, "Error getting child document", Toast.LENGTH_SHORT).show();
                    });
        });

    }

    @Override
    public int getItemCount() {
        return (list != null ? list.size() : 0);
    }

    public void setList(ArrayList<CardItem> list) {
        this.list = list;
    }
}
