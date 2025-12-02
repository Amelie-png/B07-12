package com.example.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainNavActivity extends AppCompatActivity {

    private String uid;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav);

        // Receive uid + role
        uid = getIntent().getStringExtra("uid");
        role = getIntent().getStringExtra("role");

        if (uid == null || role == null) {
            Toast.makeText(this, "Error: Missing user data", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_sign_out) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            }
            return false;
        });

        BottomNavigationView bottomNav = findViewById(R.id.main_bottom_nav);

        // Clear old menu
        bottomNav.getMenu().clear();

        // Load correct menu
        switch (role) {
            case "child":
                bottomNav.inflateMenu(R.menu.bottom_nav_menu_child);
                break;
            case "provider":
                bottomNav.inflateMenu(R.menu.bottom_nav_menu_provider);
                break;
            default:
                bottomNav.inflateMenu(R.menu.bottom_nav_menu_parent);
        }

        // Nav Host
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.main_nav_host_fragment);

        NavController navController = navHostFragment.getNavController();

        // Choose correct graph
        int graph;
        switch (role) {
            case "child":
                graph = R.navigation.nav_graph_child;
                break;
            case "provider":
                graph = R.navigation.nav_graph_provider;
                break;
            default:
                graph = R.navigation.nav_graph_parent;
        }

        // Set initial graph
        Bundle args = new Bundle();
        args.putString("uid", uid);
        args.putString("role", role);
        args.putString("providerUid", getIntent().getStringExtra("providerUid"));  // ⭐ FIX

        navController.setGraph(graph, args);

        // -------------------------------------------------
        // FIX: Highlight correct Home tab on screen load
        // -------------------------------------------------
        switch (role) {
            case "child":
                bottomNav.setSelectedItemId(R.id.childHomeFragment);
                break;
            case "provider":
                bottomNav.setSelectedItemId(R.id.providerHomeFragment);
                break;
            default:
                bottomNav.setSelectedItemId(R.id.parentHomeFragment);
                break;
        }

        // ------------ UNIVERSAL NAVIGATION HANDLER -------------
        bottomNav.setOnItemSelectedListener(item -> {

            int dest = item.getItemId();

            Bundle passArgs = new Bundle();
            passArgs.putString("uid", uid);
            passArgs.putString("role", role);

            try {
                navController.navigate(dest, passArgs);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }
}
