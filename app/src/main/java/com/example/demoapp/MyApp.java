package com.example.demoapp;

// MyApp.java
import android.app.Application;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyApp extends Application {
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
    }

    public FirebaseFirestore getDb() {
        return db;
    }
}
