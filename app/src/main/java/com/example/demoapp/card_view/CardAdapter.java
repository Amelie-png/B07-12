package com.example.demoapp.card_view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.MainActivity;
import com.example.demoapp.MainNavActivity;
import com.example.demoapp.R;
import android.widget.Toast;

import java.util.ArrayList;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    public class CardViewHolder extends RecyclerView.ViewHolder {
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

    public CardAdapter(Activity activity, ArrayList<CardItem> list) {
        this.activity = activity;
        this.list = list;
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

        // Handle click
        holder.cardRoot.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), item.name + " clicked", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(activity, MainNavActivity.class);
            intent.putExtra("uid", item.childId);
            intent.putExtra("role", "provider");

            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        if(list != null){
            return list.size();
        }else{
            return 0;
        }
    }

    public void setList(ArrayList<CardItem> list){
        this.list = list;
    }
}
