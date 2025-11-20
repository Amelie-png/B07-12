package com.example.demoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TrophyAdapter extends RecyclerView.Adapter<TrophyAdapter.TrophyViewHolder> {

    private List<Trophy> trophies;

    public TrophyAdapter(List<Trophy> trophies) {
        this.trophies = trophies;
    }

    @NonNull
    @Override
    public TrophyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trophy, parent, false);
        return new TrophyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrophyViewHolder holder, int position) {
        Trophy trophy = trophies.get(position);
        holder.icon.setImageResource(trophy.iconResId);
        holder.month.setText(trophy.month);
    }

    @Override
    public int getItemCount() {
        return trophies.size();
    }

    static class TrophyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView month;

        public TrophyViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.imageTrophy);
            month = itemView.findViewById(R.id.trophyMonth);
        }
    }
}
