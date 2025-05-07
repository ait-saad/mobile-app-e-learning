package com.projet.skilllearn.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projet.skilllearn.model.Achievement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire des progrès utilisateur
 * Utilise le pattern Singleton pour un accès global
 */
public class UserProgressManager {

    private static UserProgressManager instance;
    private final FirebaseDatabase database;
    private final FirebaseAuth auth;
    private ProgressUpdateListener progressUpdateListener;

    /**
     * Initialise les données de progression pour un nouvel utilisateur
     * @param userId ID de l'utilisateur
     */
    public void initializeUserProgress(String userId) {
        if (userId == null || userId.isEmpty()) {
            return;
        }

        DatabaseReference userProgressRef = database.getReference("user_progress").child(userId);

        // Vérifier si l'utilisateur a déjà des données de progression
        userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Créer un nœud vide pour la progression de l'utilisateur
                    userProgressRef.setValue(new HashMap<>());

                    // Créer un badge pour le premier jour
                    Achievement firstDayAchievement = new Achievement(
                            "first_day",
                            "Premier jour",
                            "Bienvenue sur SkillLearn !",
                            "milestone",
                            System.currentTimeMillis()
                    );

                    // Ajouter le badge
                    addAchievement(firstDayAchievement);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gérer l'erreur si nécessaire
            }
        });
    }

    /**
     * Interface pour les notifications de mise à jour de progrès
     */
    public interface ProgressUpdateListener {
        void onProgressUpdated(String courseId, int percentage);
    }

    /**
     * Interface pour récupérer le progrès d'un cours
     */
    public interface CourseProgressCallback {
        void onProgressLoaded(int percentage);
        void onError(String errorMessage);
    }

    /**
     * Interface pour récupérer les cours en cours
     */
    public interface UserCoursesCallback {
        void onCoursesLoaded(List<String> courseIds);
        void onError(String errorMessage);
    }

    /**
     * Interface pour récupérer les succès
     */
    public interface AchievementsCallback {
        void onAchievementsLoaded(List<Achievement> achievements);
        void onError(String errorMessage);
    }

    /**
     * Constructeur privé (pattern Singleton)
     */
    private UserProgressManager() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Obtient l'instance unique
     * @return l'instance de UserProgressManager
     */
    public static synchronized UserProgressManager getInstance() {
        if (instance == null) {
            instance = new UserProgressManager();
        }
        return instance;
    }

    /**
     * Définit un écouteur pour les mises à jour de progrès
     * @param listener l'écouteur
     */
    public void setProgressUpdateListener(ProgressUpdateListener listener) {
        this.progressUpdateListener = listener;
    }

    /**
     * Met à jour la progression d'un cours
     * @param courseId ID du cours
     * @param percentage pourcentage de progression (0-100)
     */
    public void updateCourseProgress(String courseId, int percentage) {
        // Vérifier si l'utilisateur est connecté
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference progressRef = database.getReference("user_progress")
                .child(userId).child(courseId);

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("percentage", percentage);
        progressData.put("lastUpdated", System.currentTimeMillis());

        progressRef.updateChildren(progressData).addOnSuccessListener(unused -> {
            // Notifier les écouteurs
            if (progressUpdateListener != null) {
                progressUpdateListener.onProgressUpdated(courseId, percentage);
            }

            // Vérifier si le cours est complété
            if (percentage >= 100) {
                checkCourseCompletion(courseId);
            }
        });
    }

    /**
     * Marque une section d'un cours comme complétée
     * @param courseId ID du cours
     * @param sectionId ID de la section
     * @param totalSections nombre total de sections dans le cours
     */
    public void markSectionCompleted(String courseId, String sectionId, int totalSections) {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference sectionRef = database.getReference("user_progress")
                .child(userId).child(courseId).child("sections").child(sectionId);

        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("completed", true);
        sectionData.put("completedAt", System.currentTimeMillis());

        sectionRef.setValue(sectionData).addOnSuccessListener(unused -> {
            // Mettre à jour la progression globale
            updateSectionProgress(courseId, totalSections);
        });
    }

    /**
     * Récupère le progrès d'un utilisateur pour un cours spécifique
     * @param courseId ID du cours
     * @param callback callback pour le résultat
     */
    public void getCourseProgress(String courseId, CourseProgressCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference progressRef = database.getReference("user_progress")
                .child(userId).child(courseId);

        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("percentage")) {
                    Integer percentage = snapshot.child("percentage").getValue(Integer.class);
                    if (percentage != null) {
                        callback.onProgressLoaded(percentage);
                    } else {
                        callback.onProgressLoaded(0);
                    }
                } else {
                    callback.onProgressLoaded(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Récupère la liste des IDs de cours auxquels l'utilisateur est inscrit
     * @param callback callback pour le résultat
     */
    public void getUserCourses(UserCoursesCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference progressRef = database.getReference("user_progress").child(userId);

        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> courseIds = new ArrayList<>();
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    courseIds.add(courseSnapshot.getKey());
                }
                callback.onCoursesLoaded(courseIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Récupère les succès de l'utilisateur
     * @param callback callback pour le résultat
     */
    public void getUserAchievements(AchievementsCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference achievementsRef = database.getReference("user_achievements").child(userId);

        achievementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Achievement> achievements = new ArrayList<>();
                for (DataSnapshot achievementSnapshot : snapshot.getChildren()) {
                    Achievement achievement = achievementSnapshot.getValue(Achievement.class);
                    if (achievement != null) {
                        achievement.setId(achievementSnapshot.getKey());
                        achievements.add(achievement);
                    }
                }
                callback.onAchievementsLoaded(achievements);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Inscrit un utilisateur à un cours
     * @param courseId ID du cours
     */
    public Task<Void> enrollInCourse(String courseId) {
        if (auth.getCurrentUser() == null) {
            throw new IllegalStateException("Utilisateur non connecté");
        }

        String userId = auth.getCurrentUser().getUid();

        // Mettre à jour le progrès utilisateur
        DatabaseReference userProgressRef = database.getReference("user_progress")
                .child(userId).child(courseId);

        Map<String, Object> initialData = new HashMap<>();
        initialData.put("percentage", 0);
        initialData.put("enrolledAt", System.currentTimeMillis());

        // Mettre à jour le nombre d'inscrits au cours
        DatabaseReference courseRef = database.getReference("courses")
                .child(courseId).child("enrolledCount");

        courseRef.get().addOnSuccessListener(dataSnapshot -> {
            Long currentCount = dataSnapshot.getValue(Long.class);
            if (currentCount == null) {
                currentCount = 0L;
            }
            courseRef.setValue(currentCount + 1);
        });

        // Vérifier si c'est le premier cours
        checkFirstCourseAchievement();

        return userProgressRef.setValue(initialData);
    }

    /**
     * Met à jour la progression après avoir complété une section
     * @param courseId ID du cours
     * @param totalSections nombre total de sections
     */
    private void updateSectionProgress(String courseId, int totalSections) {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference sectionsRef = database.getReference("user_progress")
                .child(userId).child(courseId).child("sections");

        sectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int completedSections = 0;

                for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                    Boolean completed = sectionSnapshot.child("completed").getValue(Boolean.class);
                    if (completed != null && completed) {
                        completedSections++;
                    }
                }

                int percentage = (int) ((float) completedSections / totalSections * 100);
                updateCourseProgress(courseId, percentage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gérer l'erreur
            }
        });
    }

    /**
     * Vérifie si un cours est complété et décerne un badge si nécessaire
     * @param courseId ID du cours
     */
    private void checkCourseCompletion(String courseId) {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference courseRef = database.getReference("courses").child(courseId);

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String courseTitle = snapshot.child("title").getValue(String.class);
                if (courseTitle != null) {
                    // Créer un succès pour le cours complété
                    Achievement achievement = new Achievement(
                            "course_completed_" + courseId,
                            "Cours terminé : " + courseTitle,
                            "Vous avez terminé le cours avec succès",
                            "course_completion",
                            System.currentTimeMillis()
                    );

                    // Ajouter le succès
                    addAchievement(achievement);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gérer l'erreur
            }
        });
    }

    /**
     * Vérifie si c'est le premier cours de l'utilisateur et décerne un badge
     */
    private void checkFirstCourseAchievement() {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference progressRef = database.getReference("user_progress").child(userId);

        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 1) {
                    // C'est le premier cours
                    Achievement achievement = new Achievement(
                            "first_course",
                            "Premier pas",
                            "Vous avez commencé votre premier cours",
                            "milestone",
                            System.currentTimeMillis()
                    );

                    // Ajouter le succès
                    addAchievement(achievement);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gérer l'erreur
            }
        });
    }

    /**
     * Ajoute un badge de réussite
     * @param achievement le succès à ajouter
     */
    public void addAchievement(Achievement achievement) {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference achievementRef = database.getReference("user_achievements")
                .child(userId).child(achievement.getId());

        // Vérifier si le succès existe déjà
        achievementRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Ajouter le succès s'il n'existe pas
                    achievementRef.setValue(achievement);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gérer l'erreur
            }
        });
    }
}