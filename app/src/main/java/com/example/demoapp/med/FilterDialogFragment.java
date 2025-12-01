package com.example.demoapp.med;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import com.example.demoapp.R;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.DateFormat;
import java.util.Date;

public class FilterDialogFragment extends DialogFragment {

    public interface OnFilterApplied {
        void onApply(FilterState newState);
        void onClear();
    }

    private FilterState workingCopy;
    private OnFilterApplied callback;
    private boolean isProvider;

    public FilterDialogFragment(FilterState existing, OnFilterApplied callback, boolean isProvider) {
        this.workingCopy = existing;
        this.callback = callback;
        this.isProvider = isProvider;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_filter_medicine, null);

        // Interactive UI
        RadioGroup typeGroup = view.findViewById(R.id.rg_filter_type);
        Button dateBtn = view.findViewById(R.id.btn_date_picker);
        Button applyBtn = view.findViewById(R.id.btn_apply);
        Button clearBtn = view.findViewById(R.id.btn_clear);

        //Dynamic UI
        TextView startDate = view.findViewById(R.id.tv_filter_start_date);
        TextView endDate = view.findViewById(R.id.tv_filter_end_date);

        //Hide type filters if user is provider
        if (isProvider) {
            typeGroup.setVisibility(View.GONE);
        }

        // Restore previous state
        if (workingCopy.medType != null) {
            if (workingCopy.medType.equals("controller")) {
                typeGroup.check(R.id.radio_controller);
            } else {
                typeGroup.check(R.id.radio_rescue);
            }
        }

        updateDateUI(startDate, endDate);

        dateBtn.setOnClickListener(v -> {
            MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder2 =
                    MaterialDatePicker.Builder.dateRangePicker();
            MaterialDatePicker<Pair<Long, Long>> picker = builder2.build();
            picker.addOnPositiveButtonClickListener(sel -> {
                workingCopy.dateFrom = sel.first;
                workingCopy.dateTo = sel.second;
                updateDateUI(startDate, endDate);
            });
            picker.show(getParentFragmentManager(), "PICK_DATES");
        });

        applyBtn.setOnClickListener(v -> {
            int selected = typeGroup.getCheckedRadioButtonId();
            if (selected == R.id.radio_controller)
                workingCopy.medType = "controller";
            else if (selected == R.id.radio_rescue)
                workingCopy.medType = "rescue";
            else
                workingCopy.medType = null;

            callback.onApply(workingCopy);
            dismiss();
        });

        clearBtn.setOnClickListener(v -> {
            callback.onClear();
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }

    private void updateDateUI(TextView startDate, TextView endDate){
        String startHeader = "Start date: ";
        String endHeader = "End date: ";
        //Set start date text
        if (workingCopy.dateFrom == null) {
            startDate.setText(startHeader + "Any");
        }
        else{
            startDate.setText(
                    startHeader + DateFormat.getDateInstance().format(new Date(workingCopy.dateFrom))
            );
        }

        //Set end date text
        if (workingCopy.dateFrom == null){
            endDate.setText(endHeader + "Any");
        }
        else {
            endDate.setText(
                    endHeader + DateFormat.getDateInstance().format(new Date(workingCopy.dateTo))
            );
        }
    }
}

