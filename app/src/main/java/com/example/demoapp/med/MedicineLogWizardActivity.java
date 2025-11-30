package com.example.demoapp.med;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demoapp.R;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MedicineLogWizardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MedicineLogWizardFragment extends Fragment {
    //vm
    private MedicineLogWizardViewModel vm;
    //layout
    private FrameLayout container;
    private ImageButton btnClose;
    private Button btnBack;
    //views
    private int currentStep = 0;
    private final int STEP_SELECT_MED = 0;
    private final int STEP_PRECHECK = 1;
    private final int STEP_TECHNIQUE_HELPER = 2; // dynamic index handled inside
    private final int STEP_POSTCHECK = 3;
    private final int STEP_CONFIRM = 4;
    //repo
    private MedicineRepository repo;
    //log author
    private static final String ARG_AUTHOR = "logAuthor";
    private String logAuthor;
    //child id
    private static final String ARG_CHILD = "childId";
    private String childId;

    public MedicineLogWizardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param childId String object.
     * @param logAuthor String object.
     * @return A new instance of fragment MedicineLogWizardFragment.
     */
    public static MedicineLogWizardFragment newInstance(String childId, String logAuthor) {
        MedicineLogWizardFragment f = new MedicineLogWizardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD, childId);
        args.putString(ARG_AUTHOR, logAuthor);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_medicine_log_wizard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        repo = new MedicineRepository();
        if (getArguments() != null) {
            childId = getArguments().getString(ARG_CHILD);
            logAuthor = getArguments().getString(ARG_AUTHOR);
        }
        vm = new ViewModelProvider(requireActivity()).get(MedicineLogWizardViewModel.class);

        container = root.findViewById(R.id.wizard_container);
        btnClose = root.findViewById(R.id.btn_wizard_close);
        btnBack = root.findViewById(R.id.btn_wizard_back);

        // close (X) button - confirm cancel
        btnClose.setOnClickListener(v -> confirmCancelWizard());
        btnBack.setOnClickListener(v -> onBackPressed());

        // Start at select med
        currentStep = STEP_SELECT_MED;
        showCurrentStep();
    }

    private void showCurrentStep() {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        switch (currentStep) {
            case STEP_SELECT_MED:
                View select = inflater.inflate(R.layout.view_select_medicine, container, false);
                setupSelectMedView(select);
                container.addView(select);
                break;
            case STEP_PRECHECK:
                View pre = inflater.inflate(R.layout.view_precheck, container, false);
                setupPrecheckView(pre);
                container.addView(pre);
                break;
            case STEP_TECHNIQUE_HELPER:
                View tHelper = inflater.inflate(R.layout.view_technique_helper, container, false);
                setupTechniqueHelper(tHelper);
                container.addView(tHelper);
                break;
            case STEP_POSTCHECK:
                View post = inflater.inflate(R.layout.view_postcheck, container, false);
                setupPostcheckView(post);
                container.addView(post);
                break;
            case STEP_CONFIRM:
                View confirm = inflater.inflate(R.layout.view_confirmation, container, false);
                setupConfirmationView(confirm);
                container.addView(confirm);
                break;
        }
    }

    private void setupSelectMedView(View select){
        Button next = select.findViewById(R.id.btn_select_next);
        MaterialButtonToggleGroup toggleType = select.findViewById(R.id.tg_med_type);

        //Next start as disabled
        next.setEnabled(false);

        //toggle group action
        toggleType.addOnButtonCheckedListener(
                new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                    @Override
                    public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {

                        // Disable when no selection
                        boolean hasSelection = group.getCheckedButtonId() != View.NO_ID;
                        next.setEnabled(hasSelection);

                        if (!hasSelection) {
                            vm.setSelectedMedType(null);
                            return;
                        }

                        if (!isChecked) return;

                        String medType = null;
                        if (checkedId == R.id.btn_controller) {
                            medType = "controller";
                        } else if (checkedId == R.id.btn_rescue) {
                            medType = "rescue";
                        }

                        vm.setSelectedMedType(medType);
                    }
                }
        );

        //UI save user checked option
        vm.getSelectedMedType().observe(getViewLifecycleOwner(), type -> {
            if (type == null) {
                toggleType.clearChecked();
            } else if (type.equals("controller")) {
                toggleType.check(R.id.btn_controller);
            } else if (type.equals("rescue")) {
                toggleType.check(R.id.btn_rescue);
            }
        });

        next.setOnClickListener(b -> {
                currentStep = STEP_PRECHECK;
                showCurrentStep();
        });
    }

    private void setupPrecheckView(View pre) {
        Button next = pre.findViewById(R.id.btn_pre_next);
        SeekBar rate = pre.findViewById(R.id.rb_pre_breathing);

        //Seek bar (slider) action
        rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int currentValue, boolean fromUser) {
                if (fromUser) {
                    vm.setPreBreathRating(currentValue);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //UI save user set value
        vm.getPreBreathRating().observe(getViewLifecycleOwner(), rate::setProgress);

        next.setOnClickListener(b -> {
            currentStep = STEP_TECHNIQUE_HELPER;
            showCurrentStep();
        });
    }

    private void setupTechniqueHelper(View tHelper) {
        CheckBox step1 = tHelper.findViewById(R.id.cb_step1);
        CheckBox step2 = tHelper.findViewById(R.id.cb_step2);
        CheckBox step3 = tHelper.findViewById(R.id.cb_step3);
        CheckBox step4 = tHelper.findViewById(R.id.cb_step4);
        CheckBox step5 = tHelper.findViewById(R.id.cb_step5);
        Button next = tHelper.findViewById(R.id.btn_technique_continue);

        List<CheckBox> allSteps = Arrays.asList(
                step1, step2, step3, step4, step5
        );

        CompoundButton.OnCheckedChangeListener listener = (button, isChecked) -> {
            // Save to ViewModel
            List<Boolean> state = new ArrayList<>();
            for (CheckBox cb : allSteps) state.add(cb.isChecked());
            vm.setTechniqueSteps(state);
        };

        for (CheckBox cb : allSteps) cb.setOnCheckedChangeListener(listener);

        // UI save user checked steps
        vm.getTechniqueSteps().observe(getViewLifecycleOwner(), state -> {
            if (state != null && state.size() == allSteps.size()) {
                for (int i = 0; i < allSteps.size(); i++) {
                    allSteps.get(i).setChecked(state.get(i));
                }
            }
        });

        next.setOnClickListener(b -> {
            currentStep = STEP_POSTCHECK;
            showCurrentStep();
        });
    }

    private void setupPostcheckView(View post) {
        Button next = post.findViewById(R.id.btn_post_next);
        MaterialButtonToggleGroup toggleState = post.findViewById(R.id.tg_condition_change);
        SeekBar rate = post.findViewById(R.id.rb_post_breathing);

        //Next start as disabled
        next.setEnabled(false);

        //toggle group action
        toggleState.addOnButtonCheckedListener(
                new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                    @Override
                    public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {

                        // Disable when no selection
                        boolean hasSelection = group.getCheckedButtonId() != View.NO_ID;
                        next.setEnabled(hasSelection);

                        if (!hasSelection) {
                            vm.setConditionChange(null);
                            return;
                        }

                        if (!isChecked) return;

                        String state = null;
                        if (checkedId == R.id.btn_worse) {
                            state = "worse";
                        } else if (checkedId == R.id.btn_same) {
                            state = "same";
                        } else if (checkedId == R.id.btn_better) {
                            state = "better";
                        }

                        vm.setConditionChange(state);
                    }
                }
        );

        //UI save user checked option
        vm.getConditionChange().observe(getViewLifecycleOwner(), type -> {
            if (type == null) {
                toggleState.clearChecked();
            } else if (type.equals("worse")) {
                toggleState.check(R.id.btn_worse);
            } else if (type.equals("same")) {
                toggleState.check(R.id.btn_same);
            } else if (type.equals("better")) {
                toggleState.check(R.id.btn_better);
            }
        });

        //Seek bar action
        rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int currentValue, boolean fromUser) {
                if (fromUser) {
                    vm.setPostBreathRating(currentValue);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //UI save user set value
        vm.getPostBreathRating().observe(getViewLifecycleOwner(), rate::setProgress);

        next.setOnClickListener(b -> {
            currentStep = STEP_CONFIRM;
            showCurrentStep();
        });
    }

    private void setupConfirmationView(View conf) {
        Button confirm = conf.findViewById(R.id.btn_confirm_log);
        TextView displayType = conf.findViewById(R.id.tv_confirm_type);
        EditText dose = conf.findViewById(R.id.et_confirm_dose);
        CheckBox flagLow = conf.findViewById(R.id.cb_flag_low);

        //edit text action
        dose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String data = s.toString().trim();

                if (data.isEmpty()) {
                    vm.setDoseTaken(0);
                    return;
                }
                try {
                    int value = Integer.parseInt(data);
                    vm.setDoseTaken(value);
                } catch (NumberFormatException e) {
                    // Invalid input → ignore or clear
                    vm.setDoseTaken(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //check box action
        flagLow.setOnCheckedChangeListener((buttonView, isChecked) -> {
                vm.setFlaggedLowStock(isChecked);
        });

        displayType.setText(vm.getSelectedMedType().getValue());

        confirm.setOnClickListener(b -> {
            confirm.setEnabled(false);
            submitLogToFirestore();});
    }

    private void submitLogToFirestore() {
        MedicineEntry entry = new MedicineEntry();
        entry.setChildId(childId);
        entry.setMedType(vm.getSelectedMedType().getValue());
        entry.setTimestamp(System.currentTimeMillis());
        entry.setDoseCount(vm.getDoseTaken().getValue() != null ? vm.getDoseTaken().getValue() : 1);
        entry.setFlaggedLowStock(vm.getFlaggedLowStock().getValue() != null ? vm.getFlaggedLowStock().getValue() : false);
        entry.setTechniqueCompleted(Objects.requireNonNull(vm.techniqueSteps.getValue()));
        entry.setPreBreathRating(vm.getPreBreathRating().getValue() != null ? vm.getPreBreathRating().getValue() : 0);
        entry.setPostBreathRating(vm.getPostBreathRating().getValue() != null ? vm.getPostBreathRating().getValue() : 0);
        entry.setConditionChange(vm.getConditionChange().getValue());
        entry.setLogAuthor(logAuthor);
        repo.addMedLog(entry, new MedicineRepository.OnResult<String>() {
            @Override public void onSuccess(String id) {
                // Success -> dismiss the wizard and return to log list
                Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
            @Override public void onFailure(Exception e) {
                // show error dialog
                new AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Could not save log: " + e.getMessage())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void onBackPressed() {
        if (currentStep == STEP_SELECT_MED) {
            confirmCancelWizard();
            return;
        }
        // simple back: select med <- precheck <- technique helper <- postcheck <- confirm
        if (currentStep == STEP_TECHNIQUE_HELPER) {
            currentStep = STEP_PRECHECK;
        } else if (currentStep == STEP_POSTCHECK) {
            currentStep = STEP_TECHNIQUE_HELPER;
        } else if (currentStep == STEP_CONFIRM) {
            currentStep = STEP_POSTCHECK;
        } else if (currentStep == STEP_PRECHECK) {
            currentStep = STEP_SELECT_MED;
        }
        showCurrentStep();
    }

    private void confirmCancelWizard() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel entry?")
                .setMessage("Are you sure you want to cancel this log? Progress will be lost.")
                .setPositiveButton("Yes", (d, w) -> {
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .setNegativeButton("No", null)
                .show();
    }
}