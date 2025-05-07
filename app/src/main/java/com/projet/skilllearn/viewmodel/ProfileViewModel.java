package com.projet.skilllearn.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projet.skilllearn.model.Achievement;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<Integer> learningProgress = new MutableLiveData<>(0);
    private final MutableLiveData<List<Achievement>> achievements = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProfileViewModel() {
        // Initialiser avec des données par défaut
        initDefaultAchievements();
    }

    public LiveData<Integer> getLearningProgress() {
        return learningProgress;
    }

    public LiveData<List<Achievement>> getAchievements() {
        return achievements;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadUserData(String userId) {
        loadUserProgress(userId);
        loadUserAchievements(userId);
    }

    private void loadUserProgress(String userId) {
        DatabaseReference progressRef = FirebaseDatabase.getInstance()
                .getReference("user_progress")
                .child(userId);

        progressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int totalCourses = 0;
                int completedCourses = 0;

                for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                    totalCourses++;
                    Integer percentage = courseSnapshot.child("percentage").getValue(Integer.class);
                    if (percentage != null && percentage == 100) {
                        completedCourses++;
                    }
                }

                if (totalCourses > 0) {
                    int progress = (completedCourses * 100) / totalCourses;
                    learningProgress.setValue(progress);
                } else {
                    learningProgress.setValue(0);
                }

                // Mise à jour des achievements basés sur le progrès
                updateProgressAchievements(completedCourses);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                errorMessage.setValue(databaseError.getMessage());
            }
        });
    }

    private void loadUserAchievements(String userId) {
        DatabaseReference achievementsRef = FirebaseDatabase.getInstance()
                .getReference("user_achievements")
                .child(userId);

        achievementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Si l'utilisateur a des données d'achievements, les charger
                if (dataSnapshot.exists()) {
                    List<Achievement> userAchievements = new ArrayList<>();

                    for (DataSnapshot achievementSnapshot : dataSnapshot.getChildren()) {
                        Achievement achievement = achievementSnapshot.getValue(Achievement.class);
                        if (achievement != null) {
                            userAchievements.add(achievement);
                        }
                    }

                    achievements.setValue(userAchievements);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                errorMessage.setValue(databaseError.getMessage());
            }
        });
    }

    private void initDefaultAchievements() {
        List<Achievement> defaultAchievements = new ArrayList<>();

        // Ajouter quelques réalisations par défaut
        defaultAchievements.add(new Achievement(
                "achievement1",
                "Premier cours",
                "Compléter votre premier cours",
                "",
                true // Par défaut, débloqué
        ));

        defaultAchievements.add(new Achievement(
                "achievement2",
                "Apprenti",
                "Compléter 5 cours",
                "",
                false
        ));

        defaultAchievements.add(new Achievement(
                "achievement3",
                "Expert",
                "Compléter 10 cours",
                "",
                false
        ));

        achievements.setValue(defaultAchievements);
    }

    private void updateProgressAchievements(int completedCourses) {
        List<Achievement> currentAchievements = achievements.getValue();
        if (currentAchievements == null) return;

        boolean updated = false;

        for (Achievement achievement : currentAchievements) {
            if (achievement.getId().equals("achievement1") && completedCourses >= 1 && !achievement.isUnlocked()) {
                achievement.setUnlocked(true);
                updated = true;
            } else if (achievement.getId().equals("achievement2") && completedCourses >= 5 && !achievement.isUnlocked()) {
                achievement.setUnlocked(true);
                updated = true;
            } else if (achievement.getId().equals("achievement3") && completedCourses >= 10 && !achievement.isUnlocked()) {
                achievement.setUnlocked(true);
                updated = true;
            }
        }

        if (updated) {
            achievements.setValue(currentAchievements);
        }
    }
}