package com.example.demoapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ZoneHistoryAdapter extends RecyclerView.Adapter<ZoneHistoryAdapter.ZoneViewHolder> {

    private final List<ZoneHistoryItem> list;

    public ZoneHistoryAdapter(List<ZoneHistoryItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ZoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_zone_history, parent, false);
        return new ZoneViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ZoneViewHolder holder, int position) {
        ZoneHistoryItem item = list.get(position);

        holder.textPercent.setText(item.percent + "%");
        holder.textDate.setText(formatDate(item.timestamp));

        // --- Set zone color dot ---
        switch (item.zoneColor) {
            case "GREEN":
                holder.dot.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;
            case "YELLOW":
                holder.dot.setBackgroundColor(Color.parseColor("#FFC107"));
                break;
            case "RED":
                holder.dot.setBackgroundColor(Color.parseColor("#F44336"));
                break;
        }

        // --- NEW: Show medicine timing ---
        if (item.beforeMed) {
            holder.textMed.setText("Before medicine");
            holder.textMed.setTextColor(Color.parseColor("#2196F3")); // blue
        } else if (item.afterMed) {
            holder.textMed.setText("After medicine");
            holder.textMed.setTextColor(Color.parseColor("#9C27B0")); // purple
        } else {
            holder.textMed.setText("Unknown");
            holder.textMed.setTextColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ZoneViewHolder extends RecyclerView.ViewHolder {
        View dot;
        TextView textPercent, textDate, textMed;

        public ZoneViewHolder(@NonNull View itemView) {
            super(itemView);
            dot = itemView.findViewById(R.id.colorDot);
            textPercent = itemView.findViewById(R.id.textZonePercent);
            textDate = itemView.findViewById(R.id.textZoneDate);
            textMed = itemView.findViewById(R.id.textZoneMed);   // <-- NEW
        }
    }

    private String formatDate(long time) {
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                .format(new Date(time));
    }
}
