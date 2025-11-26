package com.example.demoapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class ChildNavActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_child);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.top_toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            // 可处理菜单点击，例如登出、设置
            return false;
        });

        // BottomNavigationView + NavController
        BottomNavigationView bottomNav = findViewById(R.id.childBottomNav);
        NavController navController = Navigation.findNavController(this, R.id.childFragmentContainer);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }
}
