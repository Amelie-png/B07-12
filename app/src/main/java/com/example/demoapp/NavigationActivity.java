package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_child);

        // 模拟获取用户类型，比如从登录返回的 intent 或 SharedPreferences
        String userType = getIntent().getStringExtra("userType"); // 返回 "parent" 或 "child"
        Toolbar toolbar = findViewById(R.id.top_toolbar);

        // 2️⃣ 设置为支持的 ActionBar
        setSupportActionBar(toolbar);
        // 根据用户类型加载不同 layout
        if ("parent".equals(userType)) {
            setContentView(R.layout.activity_navigation_parent);
        } else {
            setContentView(R.layout.activity_navigation_child);
        }

        // 获取 NavHostFragment
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            Log.e("NavCheck", "NavHostFragment is null!");
            return;
        }

        // 获取 NavController
        NavController navController = navHostFragment.getNavController();

        // Toolbar 自动更新标题
        NavigationUI.setupWithNavController(toolbar, navController);

        // Toolbar 菜单点击
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_sign_out) {
                showSignOutDialog();
                return true;
            }
            return false;
        });

        // BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // 给 BottomNavigationView 添加点击动画
        bottomNav.setOnItemSelectedListener(item -> {
            animateBottomNavItem(bottomNav, item.getItemId());
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    // 点击动画方法
    private void animateBottomNavItem(BottomNavigationView bottomNav, int itemId) {
        View itemView = bottomNav.findViewById(itemId);
        if (itemView == null) return;

        itemView.setScaleX(1f);
        itemView.setScaleY(1f);
        itemView.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(150)
                .withEndAction(() -> itemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150))
                .start();
    }

    // 退出对话框
    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(NavigationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
