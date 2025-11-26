package com.example.demoapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class NavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 假设这里你已经通过 FirebaseAuth 拿到当前用户
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String type = document.getString("type"); // "parent" 或 "child"
                        if ("parent".equals(type)) {
                            startActivity(new Intent(this, ParentActivity.class));
                        } else if ("child".equals(type)) {
                            startActivity(new Intent(this, ChildNavActivity.class));
                        }
                        finish(); // 关闭当前 NavigationActivity
                    } else {
                        // 如果找不到用户类型，可以回到登录页面
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    // 出错也返回登录
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
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
