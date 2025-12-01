package com.example.demoapp.med;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    private List<MedicineEntry> entries;

    /**
     * Provide a reference to the type of views that are used
     * (custom ViewHolder)
     */
    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        //dynamic fields
        TextView medType;
        TextView takenTime;
        TextView takenDose;
        TextView breathingRating;
        TextView conditionChange;

        public MedicineViewHolder(View view) {
            super(view);

            medType = view.findViewById(R.id.tv_medicine_type);
            takenTime = view.findViewById(R.id.tv_dose_taken);
            takenDose = view.findViewById(R.id.tv_taken_time);
            breathingRating = view.findViewById(R.id.tv_breathing_rating);
            conditionChange = view.findViewById(R.id.tv_condition_change);
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param entries List<MedicineEntry> containing the data to populate views to be used
     *                by RecyclerView
     */
    public MedicineAdapter(List<MedicineEntry> entries) {
        this.entries = entries;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_log_card, parent, false);
        return new MedicineViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        MedicineEntry item = entries.get(position);

        //dynamic data
        holder.medType.setText(item.getMedType());
        holder.takenDose.setText("Dose taken: "+ item.getDoseCount());
        holder.takenTime.setText("Taken at: " + item.getTimestamp());
        holder.breathingRating.setText("Breating rating: " + item.getPreBreathRating() + " -> " + item.getPostBreathRating());
        holder.conditionChange.setText("Condition change: " + item.getConditionChange());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void updateList(List<MedicineEntry> newList) {
        this.entries.clear();
        this.entries.addAll(newList);
        notifyDataSetChanged();
    }
}