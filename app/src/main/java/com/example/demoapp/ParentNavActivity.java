package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.firebase.auth.FirebaseAuth;

public class ParentNavActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_parent);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.top_toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_sign_out) {
                signOut();
                return true;
            }
            return false;
        });

        // BottomNavigationView + NavController
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
        // 返回登录页面
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
