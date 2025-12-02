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

/**
 * RecyclerView adapter that displays a list of symptoms and allows the user
 * to select or deselect them. This adapter supports normal symptom selection
 * as well as a special “Other” option, which prompts the user for custom input.
 *
 * <p>The adapter communicates selection changes back to the hosting UI through
 * the {@link OnSelectionChangedListener} callback interface.</p>
 *
 * <p>Each symptom is represented using a {@code Pair<String, String>} where:
 * <ul>
 *     <li>{@code first} – Canonical identifier (e.g., "Cough")</li>
 *     <li>{@code second} – Display name or custom text</li>
 * </ul>
 * This allows “Other” to store custom user-entered text.</p>
 */
public class SymptomsAdapter extends RecyclerView.Adapter<SymptomsAdapter.ViewHolder> {

    /** List of all symptom options available to the user. */
    private final ArrayList<Pair<String, String>> symptomsList;

    /**
     * List of selected symptoms.
     * Each selected symptom is stored as a Pair(first, second). For "Other",
     * the second value may contain custom text.
     */
    private final ArrayList<Pair<String, String>> selectedSymptoms;

    /** Callback to notify UI when the number of selected symptoms changes. */
    private final OnSelectionChangedListener listener;

    /** Context needed for dialogs and inflating layouts. */
    private final Context context;

    /**
     * Interface for notifying the calling Activity/Fragment whenever the selection count changes.
     */
    public interface OnSelectionChangedListener {
        /**
         * Called whenever the selected symptom count changes.
         *
         * @param count The updated number of selected symptoms.
         */
        void onSelectionChanged(int count);
    }

    /**
     * Creates an instance of the SymptomsAdapter.
     *
     * @param symptomsList The list of symptoms to display.
     * @param listener Callback listener for selection change events.
     * @param context Context used for inflating views and showing dialogs.
     */
    public SymptomsAdapter(ArrayList<Pair<String, String>> symptomsList,
                           OnSelectionChangedListener listener,
                           Context context) {
        this.symptomsList = symptomsList;
        this.selectedSymptoms = new ArrayList<>();
        this.listener = listener;
        this.context = context;
    }

    /**
     * Returns the list of selected symptoms, including any custom “Other” entries.
     *
     * @return A list of selected symptom pairs.
     */
    public ArrayList<Pair<String, String>> getSelected() {
        return selectedSymptoms;
    }

    /**
     * Inflates the symptom list item layout and returns a ViewHolder instance.
     *
     * @param parent The parent view group.
     * @param viewType The view type (unused in this adapter).
     * @return A new ViewHolder for the symptom item layout.
     */
    @NonNull
    @Override
    public SymptomsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_symptom, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds a symptom item to the ViewHolder for display.
     *
     * <p>This method:
     * <ul>
     *     <li>Shows the symptom name</li>
     *     <li>Displays selected/unselected UI state</li>
     *     <li>Handles selection/deselection clicks</li>
     *     <li>Handles “Other” (custom input) selections</li>
     * </ul>
     *
     * @param holder The ViewHolder to bind data into.
     * @param position The position of the symptom in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull SymptomsAdapter.ViewHolder holder,
                                 int position) {

        Pair<String, String> symptomPair = symptomsList.get(position);
        String symptom = symptomPair.second;

        holder.textName.setText(symptom);

        boolean isSelected = isSelected(symptomPair, selectedSymptoms);

        // Update the selection circle UI state
        holder.circle.setImageResource(
                isSelected ? R.drawable.ic_circle_selected : R.drawable.ic_circle_unselected
        );

        holder.itemView.setOnClickListener(v -> {

            // ======================================
            // SPECIAL LOGIC FOR "OTHER" CUSTOM INPUT
            // ======================================
            if (symptom.equalsIgnoreCase("Other")) {
                showCustomInputDialog();
                return;
            }

            // ================================
            // NORMAL SELECT / DESELECT LOGIC
            // ================================
            if (isSelected) {
                // Remove matching pair from selected list
                selectedSymptoms.removeIf(p -> p.first.equals(symptom) && p.second.equals(symptom));
            } else {
                selectedSymptoms.add(new Pair<>(symptom, symptom));
            }

            notifyItemChanged(position);
            listener.onSelectionChanged(selectedSymptoms.size());
        });
    }

    /**
     * Checks whether a specific pair is already contained inside the selected list.
     *
     * @param target The symptom pair to search for.
     * @param list The selected list to search through.
     * @return True if the pair is found, false otherwise.
     */
    public boolean isSelected(Pair<String, String> target, ArrayList<Pair<String, String>> list) {
        String first = target.first;
        String second = target.second;
        for (Pair<String, String> p : list) {
            if (p.first.equals(first) && p.second.equals(second)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of symptom items in the full list.
     *
     * @return Total number of symptoms displayed.
     */
    @Override
    public int getItemCount() {
        return symptomsList.size();
    }

    /**
     * ViewHolder class that holds references to the views inside each symptom row.
     * <p>Each row contains:</p>
     * <ul>
     *     <li>TextView – the symptom name</li>
     *     <li>ImageView – selection state indicator</li>
     * </ul>
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        /** Displays the symptom name. */
        TextView textName;

        /** Icon that visually shows selected/unselected state. */
        ImageView circle;

        /**
         * Creates a new ViewHolder that stores references to UI components.
         *
         * @param itemView The inflated layout for a single symptom row.
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textSymptomName);
            circle = itemView.findViewById(R.id.circleSelect);
        }
    }

    /**
     * Displays a dialog allowing the user to enter a custom symptom text
     * when selecting the “Other” option.
     *
     * <p>If the user confirms with non-empty input, the custom symptom is added
     * to the selection under the canonical "Other" category.</p>
     */
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
