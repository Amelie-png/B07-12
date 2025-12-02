package com.example.demoapp.symptoms;

/**
 * Model class representing a single symptom item that can be displayed
 * in a list or selection UI (e.g., checkboxes or toggles).
 *
 * <p>This class holds two pieces of data:</p>
 * <ul>
 *     <li><b>name</b> – the textual label of the symptom (e.g., "Cough", "Headache")</li>
 *     <li><b>selected</b> – whether the symptom is currently selected by the user</li>
 * </ul>
 *
 * <p>It is used by adapters and presenters to track user selections in the
 * Symptoms/Triggers/Daily Log features of the application.</p>
 */
public class SymptomItem {

    /** The name or label of the symptom (e.g., "Cough"). */
    private String name;

    /** Indicates whether the symptom is selected by the user. */
    private boolean selected;

    /**
     * Constructs a new {@code SymptomItem} with the given name.
     * The item is unselected by default.
     *
     * @param name The display name of the symptom.
     */
    public SymptomItem(String name) {
        this.name = name;
        this.selected = false;
    }

    /**
     * Returns the name of the symptom.
     *
     * @return The symptom name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether the symptom is selected.
     *
     * @return {@code true} if the symptom is selected, otherwise {@code false}.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Updates the selected state of the symptom.
     *
     * @param selected {@code true} to mark the symptom as selected,
     *                 {@code false} to mark it as unselected.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
