package com.example.demoapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class HomeStepsCardFragment extends Fragment {

    private CountDownTimer timer;
    private long timeLeft = 10 * 60 * 1000; // 10 minutes

    // ⭐ zone argument key + field
    private static final String ARG_ZONE = "zone";
    private String zone;

    public HomeStepsCardFragment() {}

    public static HomeStepsCardFragment newInstance(String zone) {
        HomeStepsCardFragment f = new HomeStepsCardFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ZONE, zone);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            zone = getArguments().getString(ARG_ZONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.card_home_steps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView timerTxt = view.findViewById(R.id.textTimer);
        Button backHomeBtn = view.findViewById(R.id.btnBackHome);

        // TODO: 根据 zone 显示不同 home steps
        // eg. if(zone.equals("yellow")) showYellowSteps(); else showGreenSteps();

        // start 10-minute automatic re-check timer
        startTimer(timerTxt);

        backHomeBtn.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            getParentFragmentManager().popBackStack(); // go home
        });
    }

    private void startTimer(TextView timerTxt) {
        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long ms) {
                timeLeft = ms;
                int minutes = (int) (ms / 1000) / 60;
                int seconds = (int) (ms / 1000) % 60;
                timerTxt.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerTxt.setText("00:00");
                showRecheckPopup();
            }
        };

        timer.start();
    }

    private void showRecheckPopup() {
        new AlertDialog.Builder(getContext())
                .setTitle("Re-check")
                .setMessage("Are symptoms improving?")
                .setPositiveButton("Yes", (d, w) -> {
                    getParentFragmentManager().popBackStack();
                })
                .setNegativeButton("Worse", (d, w) -> escalateToEmergency())
                .setCancelable(false)
                .show();
    }

    private void escalateToEmergency() {
        EmergencyCardFragment f = new EmergencyCardFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) timer.cancel();
    }
}
