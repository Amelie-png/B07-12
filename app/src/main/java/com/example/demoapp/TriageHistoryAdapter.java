package com.example.demoapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TriageHistoryAdapter extends RecyclerView.Adapter<TriageHistoryAdapter.TriageViewHolder> {

    private ArrayList<TriageRecord> list;

    public TriageHistoryAdapter(ArrayList<TriageRecord> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public TriageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_triage_history, parent, false);
        return new TriageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TriageViewHolder holder, int position) {
        TriageRecord r = list.get(position);

        holder.txtSeverity.setText(r.severity);

        // Severity color
        switch (r.severity) {
            case "RED":
                holder.strip.setBackgroundColor(Color.parseColor("#F44336"));
                break;
            case "YELLOW":
                holder.strip.setBackgroundColor(Color.parseColor("#FFEB3B"));
                break;
            default:
                holder.strip.setBackgroundColor(Color.parseColor("#4CAF50"));
        }

        // Format time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault());
        holder.txtDate.setText(sdf.format(new Date(r.timestamp)));

        holder.txtPEF.setText("PEF: " + r.pef);
        holder.txtRescueAttempts.setText("Rescue Attempts: " + r.rescueAttempts);

        // Symptoms
        ArrayList<String> symptoms = new ArrayList<>();
        if (r.cantSpeak) symptoms.add("Can't speak");
        if (r.retractions) symptoms.add("Retractions");
        if (r.blueLips) symptoms.add("Blue lips");
        if (r.cough) symptoms.add("Cough");
        if (r.chestTight) symptoms.add("Chest tight");

        if (symptoms.isEmpty())
            holder.txtSymptoms.setText("Symptoms: none");
        else
            holder.txtSymptoms.setText("Symptoms: " + String.join(", ", symptoms));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateData(ArrayList<TriageRecord> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public static class TriageViewHolder extends RecyclerView.ViewHolder {

        View strip;
        TextView txtSeverity, txtDate, txtPEF, txtRescueAttempts, txtSymptoms;

        public TriageViewHolder(@NonNull View itemView) {
            super(itemView);

            strip = itemView.findViewById(R.id.viewSeverityStrip);
            txtSeverity = itemView.findViewById(R.id.txtSeverity);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtPEF = itemView.findViewById(R.id.txtPEF);
            txtRescueAttempts = itemView.findViewById(R.id.txtRescueAttempts);
            txtSymptoms = itemView.findViewById(R.id.txtSymptoms);
        }
    }
}
