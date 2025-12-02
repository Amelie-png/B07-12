package com.example.demoapp.provider_report;

import com.example.demoapp.entry_db.EntryLogRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

public class CountProblemDaysModule {
    private LocalDate startDate, endDate;
    private String childId;
    private ArrayList<String> categories;

    public CountProblemDaysModule(LocalDate startDate, LocalDate endDate, String childId, ArrayList<String> categories){
        this.startDate = startDate;
        this.endDate = endDate;
        this.childId = childId;
        this.categories = categories;
    }

    public void countProblemDays(ResultListener listener) {
        EntryLogRepository repo = new EntryLogRepository();
        // 显式指定接口类型，避免模糊调用
        EntryLogRepository.OnProblemDaysCountedByCategoryListener callback =
                new EntryLogRepository.OnProblemDaysCountedByCategoryListener() {
                    @Override
                    public void onCounted(Map<String, Integer> counts) {
                        listener.onResult(counts);
                    }

                    @Override
                    public void onError(Exception e) {
                        listener.onError(e);
                    }
                };

        repo.countProblemDaysByCategories(childId, startDate, endDate, categories, callback);
    }

    public interface ResultListener {
        void onResult(Map<String, Integer> counts);
        void onError(Exception e);
    }
}
