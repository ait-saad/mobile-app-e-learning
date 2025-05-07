package com.projet.skilllearn;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;

public class SkillLearnApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
            Log.d("Firebase", "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e("Firebase", "Failed to initialize Firebase", e);
        }
    }
}