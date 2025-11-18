package com.example.demoapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class NavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);


        Button signOutButton = findViewById(R.id.btnSignOut);


        signOutButton.setOnClickListener(v -> showSignOutDialog());
    }


    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("OK", (dialog, which) -> {

                    Intent intent = new Intent(NavigationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null) // 用户取消，什么都不做
                .show();
    }
}
