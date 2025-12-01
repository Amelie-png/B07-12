package com.example.demoapp.med;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MedicineLogWizardViewModel extends ViewModel {
    // Select medicine
    private final MutableLiveData<String> selectedMedType = new MutableLiveData<>();
    public void setSelectedMedType(String type) { selectedMedType.setValue(type); }
    public MutableLiveData<String> getSelectedMedType() { return selectedMedType; }

    // Pre-check
    private final MutableLiveData<Integer> preBreathRating = new MutableLiveData<>(0);
    public void setPreBreathRating(int value) { preBreathRating.setValue(value); }
    public MutableLiveData<Integer> getPreBreathRating() { return preBreathRating; }

    // Technique helper
    public final MutableLiveData<List<Boolean>> techniqueSteps = new MutableLiveData<>(Arrays.asList(false, false, false, false, false));
    public void setTechniqueSteps(List<Boolean> state) { techniqueSteps.setValue(state); }
    public MutableLiveData<List<Boolean>> getTechniqueSteps() { return techniqueSteps; }

    // Post-check
    private final MutableLiveData<Integer> postBreathRating = new MutableLiveData<>(0);
    public void setPostBreathRating(int value){ postBreathRating.setValue(value); }
    public MutableLiveData<Integer> getPostBreathRating() { return postBreathRating; }
    private final MutableLiveData<String> conditionChange = new MutableLiveData<>("Same");
    public void setConditionChange(String state) { conditionChange.setValue(state); }
    public MutableLiveData<String> getConditionChange() { return conditionChange; }

    // Confirmation
    private final MutableLiveData<Integer> doseTaken = new MutableLiveData<>(1);
    public void setDoseTaken(int doses){ doseTaken.setValue(doses); }
    public MutableLiveData<Integer> getDoseTaken() { return doseTaken; }
    private final MutableLiveData<Boolean> flaggedLowStock = new MutableLiveData<>(false);
    public void setFlaggedLowStock(boolean low){ flaggedLowStock.setValue(low); }
    public MutableLiveData<Boolean> getFlaggedLowStock() { return flaggedLowStock; }

    // Metadata (client-side copy; final write uses server timestamp)
    private final MutableLiveData<Long> takenAtEpoch = new MutableLiveData<>(0L);
    private final MutableLiveData<String> childId = new MutableLiveData<>();
    public void setChildId(String id) { childId.setValue(id); }
    public MutableLiveData<String> getChildId() { return childId; }
    private final MutableLiveData<String> logAuthor = new MutableLiveData<>();
    public MutableLiveData<String> getLogAuthor() { return logAuthor; }
    public void setLogAuthor(String author) { logAuthor.setValue(author); }
}
