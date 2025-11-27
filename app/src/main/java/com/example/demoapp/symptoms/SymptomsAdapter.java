package com.example.demoapp.symptoms;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoapp.R;

import java.util.ArrayList;

public class SymptomsAdapter extends RecyclerView.Adapter<SymptomsAdapter.ViewHolder> {

    private final ArrayList<Pair<String, String>> symptomsList;
    private final ArrayList<Pair<String, String>> selectedSymptoms;
    private final OnSelectionChangedListener listener;
    private final Context context;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int count);
    }

    public SymptomsAdapter(ArrayList<Pair<String, String>> symptomsList,
                           OnSelectionChangedListener listener,
                           Context context) {
        this.symptomsList = symptomsList;
        this.selectedSymptoms = new ArrayList<>();
        this.listener = listener;
        this.context = context;
    }

    public ArrayList<Pair<String, String>> getSelected() {
        return selectedSymptoms;
    }

    @NonNull
    @Override
    public SymptomsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_symptom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomsAdapter.ViewHolder holder,
                                 int position) {
        Pair<String, String> symptomPair = symptomsList.get(position);
        String symptom = symptomPair.second;
        holder.textName.setText(symptom);

        boolean isSelected = isSelected(symptomPair, selectedSymptoms);

        holder.circle.setImageResource(
                isSelected ? R.drawable.ic_circle_selected : R.drawable.ic_circle_unselected
        );

        holder.itemView.setOnClickListener(v -> {

            // ------------------------
            // HANDLE "OTHER" INPUT
            // ------------------------
            if (symptom.equalsIgnoreCase("Other")) {
                showCustomInputDialog();
                return;
            }

            // ------------------------
            // NORMAL SELECT/DESELECT
            // ------------------------
            if (isSelected) {
                selectedSymptoms.removeIf(p -> p.first.equals(symptom) && p.second.equals(symptom));
            } else {
                selectedSymptoms.add(new Pair<>(symptom, symptom));
            }

            notifyItemChanged(position);
            listener.onSelectionChanged(selectedSymptoms.size());
        });
    }

    public boolean isSelected(Pair<String, String> target, ArrayList<Pair<String, String>> list){
        String first = target.first;
        String second = target.second;
        for (Pair<String, String> p : list) {
            if (p.first.equals(first) && p.second.equals(second)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return symptomsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        ImageView circle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textSymptomName);
            circle = itemView.findViewById(R.id.circleSelect);
        }
    }

    private void showCustomInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter custom symptom");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Type your symptom...");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String custom = input.getText().toString().trim();
            if (!custom.isEmpty()) {
                selectedSymptoms.add(new Pair<>("Other", custom));
                listener.onSelectionChanged(selectedSymptoms.size());
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
