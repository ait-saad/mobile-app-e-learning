package com.projet.skilllearn;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SkillLearnApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
            Log.d("Firebase", "Firebase initialized successfully");
            debugFirebaseStructure(); // Ajouter cette ligne
        } catch (Exception e) {
            Log.e("Firebase", "Failed to initialize Firebase", e);
        }
    }
    private void debugFirebaseStructure() {
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Vérifier les sections et leurs quizId
                Log.d("Firebase Debug", "Sections structure:");
                for (DataSnapshot sectionSnap : snapshot.child("sections").getChildren()) {
                    String sectionId = sectionSnap.getKey();
                    String quizId = sectionSnap.child("quizId").getValue(String.class);
                    Log.d("Firebase Debug", "Section: " + sectionId + ", QuizId: " + quizId);
                }

                // Vérifier les quiz
                Log.d("Firebase Debug", "Quizzes structure:");
                for (DataSnapshot quizSnap : snapshot.child("quizzes").getChildren()) {
                    String quizId = quizSnap.getKey();
                    String title = quizSnap.child("title").getValue(String.class);
                    int questionsCount = (int) quizSnap.child("questions").getChildrenCount();
                    Log.d("Firebase Debug", "Quiz: " + quizId + ", Title: " + title +
                            ", Questions count: " + questionsCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase Debug", "Debug cancelled: " + error.getMessage());
            }
        });
    }
}