package com.projet.skilllearn;
import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SkillLearnApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // Activer la persistance hors-ligne
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            // Configurer la mise en cache
            DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference("courses");
            coursesRef.keepSynced(true);

            DatabaseReference sectionsRef = FirebaseDatabase.getInstance().getReference("sections");
            sectionsRef.keepSynced(true);

            // Initialiser Firebase
            FirebaseApp.initializeApp(this);
            Log.d("Firebase", "Firebase initialisé avec succès (persistance activée)");
        } catch (Exception e) {
            Log.e("Firebase", "Échec d'initialisation de Firebase", e);
        }
    }
}