package com.example.demoapp.card_view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;
import android.widget.Toast;

import java.util.ArrayList;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    public class CardViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView profilePic;
        ImageView profileBackground;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.card_view_patient_name);
            profilePic = itemView.findViewById(R.id.card_view_patient_profile_pic);
            profileBackground = itemView.findViewById(R.id.cardview_patient_background);
        }
    }
    private ArrayList<CardItem> list;

    public CardAdapter(ArrayList<CardItem> list) {
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
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), item.name + " clicked", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
