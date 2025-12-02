package com.example.demoapp.med;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class MedicineViewModel extends ViewModel {
    private final MutableLiveData<String> childId = new MutableLiveData<>();

    private final MutableLiveData<LocalDate> purchaseDate = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> expiryDate = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> startDate = new MutableLiveData<>();

    private final MutableLiveData<Integer> remainingDoses = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalDoses = new MutableLiveData<>();

    private final MutableLiveData<Integer> dailyDose = new MutableLiveData<>();
    public final MutableLiveData<List<String>> scheduledDays = new MutableLiveData<>();

    private final MutableLiveData<Boolean> lowStock = new MutableLiveData<>();
    private final MutableLiveData<String> flagAuthor = new MutableLiveData<>();

    //Setters
    public void setChildId(String id) { childId.setValue(id); }

    public void setPurchaseDate(LocalDate date) { purchaseDate.setValue(date); }
    public void setExpiryDate(LocalDate date) { expiryDate.setValue(date); }
    public void setStartDate(LocalDate date) { startDate.setValue(date); }

    public void setRemainingDoses(int value){ remainingDoses.setValue(value); }
    public void setTotalDoses(int value) { totalDoses.setValue(value); }

    public void setDailyDose(int value) { dailyDose.setValue(value); }
    public void setScheduledDays(List<String> days) { scheduledDays.setValue(days); }

    public void isLowStock(boolean low){ lowStock.setValue(low); }
    public void setFlagAuthor(String author) { flagAuthor.setValue(author); }

    //Getters
    public MutableLiveData<String> getChildId() { return childId; }

    public MutableLiveData<LocalDate> getPurchaseDate() { return purchaseDate; }
    public MutableLiveData<LocalDate> getExpiryDate() { return expiryDate; }
    public MutableLiveData<LocalDate> getStartDate() { return startDate; }

    public MutableLiveData<Integer> getRemainingDoses() { return remainingDoses; }
    public MutableLiveData<Integer> getTotalDoses() { return totalDoses; }

    public MutableLiveData<Integer> getDailyDose() { return dailyDose; }
    public MutableLiveData<List<String>> getScheduledDays() { return scheduledDays; }

    public MutableLiveData<Boolean> getLowStock() { return lowStock; }
    public MutableLiveData<String> getFlagAuthor() { return flagAuthor; }
}
