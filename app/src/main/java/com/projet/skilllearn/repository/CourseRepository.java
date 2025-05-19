package com.projet.skilllearn.repository;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projet.skilllearn.model.Course;
import com.projet.skilllearn.model.CourseSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repository pour accéder et gérer les données des cours dans Firebase
 */
public class CourseRepository {
    private static final String TAG = "CourseRepository";
    private final DatabaseReference coursesRef;
    private final DatabaseReference sectionsRef;

    /**
     * Interface de callback pour récupérer les résultats des opérations
     */
    public interface CoursesCallback {
        void onCoursesLoaded(List<Course> courses);
        void onError(String message);
    }

    /**
     * Interface de callback pour les opérations sur un seul cours
     */
    public interface CourseCallback {
        void onCourseLoaded(Course course);
        void onError(String message);
    }

    /**
     * Interface pour récupérer les catégories
     */
    interface OnCategoryLoadedListener {
        void onCategoryLoaded(String category);
    }

    /**
     * Constructeur
     */
    public CourseRepository() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            coursesRef = database.getReference("courses");
            sectionsRef = database.getReference("sections");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation du repository", e);
            throw e; // Propager l'exception pour que l'appelant sache qu'il y a eu un problème
        }
    }

    /**
     * Récupère tous les cours dans la base de données
     * @param callback Callback pour récupérer les résultats
     */
    public void getAllCourses(CoursesCallback callback) {
        try {
            coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        List<Course> courses = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                Course course = snapshot.getValue(Course.class);
                                if (course != null) {
                                    course.setCourseId(snapshot.getKey());
                                    courses.add(course);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur lors de la conversion d'un cours", e);
                                // Continuer avec le cours suivant
                            }
                        }
                        callback.onCoursesLoaded(courses);
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors du traitement des cours", e);
                        callback.onError("Erreur de traitement: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Requête annulée", databaseError.toException());
                    callback.onError(databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique lors de la récupération des cours", e);
            callback.onError("Erreur critique: " + e.getMessage());
        }
    }

    /**
     * Récupère un cours par son ID, y compris ses sections
     * @param courseId ID du cours
     * @param callback Callback pour récupérer le résultat
     */
    public void getCourseById(String courseId, CourseCallback callback) {
        try {
            // Vérifier l'ID du cours
            if (courseId == null || courseId.isEmpty()) {
                callback.onError("ID de cours invalide");
                return;
            }

            // Récupérer les détails du cours
            coursesRef.child(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Course course = snapshot.getValue(Course.class);
                        if (course == null) {
                            callback.onError("Cours non trouvé");
                            return;
                        }

                        course.setCourseId(snapshot.getKey());

                        // Charger les sections du cours
                        loadSectionsForCourse(course, callback);
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de la récupération du cours", e);
                        callback.onError("Erreur: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Requête annulée", error.toException());
                    callback.onError(error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique lors de la récupération du cours", e);
            callback.onError("Erreur critique: " + e.getMessage());
        }
    }

    /**
     * Charge les sections d'un cours
     * @param course Le cours pour lequel charger les sections
     * @param callback Callback pour le résultat
     */
    private void loadSectionsForCourse(Course course, CourseCallback callback) {
        try {
            sectionsRef.orderByChild("courseId").equalTo(course.getCourseId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                List<CourseSection> sections = new ArrayList<>();

                                for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                                    CourseSection section = sectionSnapshot.getValue(CourseSection.class);
                                    if (section != null) {
                                        section.setSectionId(sectionSnapshot.getKey());
                                        sections.add(section);
                                    }
                                }

                                // Trier les sections par ordre
                                Collections.sort(sections, (s1, s2) ->
                                        Integer.compare(s1.getOrderIndex(), s2.getOrderIndex()));

                                course.setSections(sections);
                                callback.onCourseLoaded(course);
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur lors du chargement des sections", e);
                                // Retourner le cours même sans les sections
                                callback.onCourseLoaded(course);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Chargement des sections annulé", error.toException());
                            // Retourner le cours même sans les sections
                            callback.onCourseLoaded(course);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement des sections", e);
            // Retourner le cours même sans les sections
            callback.onCourseLoaded(course);
        }
    }

    /**
     * Récupère les cours par niveau de difficulté
     * @param level Niveau de difficulté (débutant, intermédiaire, expert)
     * @param callback Callback pour le résultat
     */
    public void getCoursesByLevel(String level, CoursesCallback callback) {
        try {
            coursesRef.orderByChild("level").equalTo(level)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                List<Course> courses = new ArrayList<>();
                                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                                    Course course = courseSnapshot.getValue(Course.class);
                                    if (course != null) {
                                        course.setCourseId(courseSnapshot.getKey());
                                        courses.add(course);
                                    }
                                }
                                callback.onCoursesLoaded(courses);
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur de traitement des cours par niveau", e);
                                callback.onError("Erreur: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Requête annulée", error.toException());
                            callback.onError(error.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique", e);
            callback.onError("Erreur critique: " + e.getMessage());
        }
    }

    /**
     * Récupère les cours triés par popularité
     * @param callback Callback pour le résultat
     */
    public void getCoursesByPopularity(CoursesCallback callback) {
        try {
            coursesRef.orderByChild("enrolledCount").limitToLast(20)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                List<Course> courses = new ArrayList<>();
                                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                                    Course course = courseSnapshot.getValue(Course.class);
                                    if (course != null) {
                                        course.setCourseId(courseSnapshot.getKey());
                                        courses.add(course);
                                    }
                                }

                                // Inverser pour avoir les plus populaires en premier
                                Collections.reverse(courses);
                                callback.onCoursesLoaded(courses);
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur de traitement des cours populaires", e);
                                callback.onError("Erreur: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Requête annulée", error.toException());
                            callback.onError(error.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique", e);
            callback.onError("Erreur critique: " + e.getMessage());
        }
    }

    /**
     * Récupère les cours populaires - méthode utilisée comme fallback
     * @param callback Callback pour le résultat
     */
    private void getPopularCourses(CoursesCallback callback) {
        try {
            coursesRef.orderByChild("enrolledCount").limitToLast(10)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                List<Course> courses = new ArrayList<>();
                                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                                    Course course = courseSnapshot.getValue(Course.class);
                                    if (course != null) {
                                        course.setCourseId(courseSnapshot.getKey());
                                        courses.add(course);
                                    }
                                }

                                // Inverser pour avoir les plus populaires en premier
                                Collections.reverse(courses);
                                callback.onCoursesLoaded(courses);
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur lors du traitement des cours populaires", e);
                                callback.onError("Erreur: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Requête annulée", error.toException());
                            callback.onError(error.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique", e);
            callback.onError("Erreur critique: " + e.getMessage());
        }
    }

    /**
     * Récupère les cours auxquels un utilisateur est inscrit
     * @param userId ID de l'utilisateur
     * @param callback Callback pour le résultat
     */
    public void getUserCourses(String userId, CoursesCallback callback) {
        try {
            // Valider l'ID utilisateur
            if (userId == null || userId.isEmpty()) {
                callback.onError("ID utilisateur invalide");
                return;
            }

            DatabaseReference userProgressRef = FirebaseDatabase.getInstance().getReference("user_progress")
                    .child(userId);

            userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (!snapshot.exists() || !snapshot.hasChildren()) {
                            callback.onCoursesLoaded(new ArrayList<>());
                            return;
                        }

                        List<Course> userCourses = new ArrayList<>();
                        AtomicInteger coursesProcessed = new AtomicInteger(0);
                        int totalCourses = (int) snapshot.getChildrenCount();

                        for (DataSnapshot courseProgress : snapshot.getChildren()) {
                            String courseId = courseProgress.getKey();
                            if (courseId == null) {
                                // Incrémenter le compteur même pour les cours sans ID
                                if (coursesProcessed.incrementAndGet() == totalCourses) {
                                    callback.onCoursesLoaded(userCourses);
                                }
                                continue;
                            }

                            getCourseById(courseId, new CourseCallback() {
                                @Override
                                public void onCourseLoaded(Course course) {
                                    try {
                                        // Ajouter la progression à l'objet cours
                                        if (courseProgress.child("percentage").exists()) {
                                            Integer percentage = courseProgress.child("percentage").getValue(Integer.class);
                                            if (percentage != null) {
                                                course.setUserProgress(percentage);
                                            }
                                        }

                                        // Ajouter le timestamp de dernière étude
                                        if (courseProgress.child("lastUpdated").exists()) {
                                            Long lastUpdated = courseProgress.child("lastUpdated").getValue(Long.class);
                                            if (lastUpdated != null) {
                                                course.setLastStudiedTimestamp(lastUpdated);
                                            }
                                        }

                                        course.setEnrolled(true);
                                        userCourses.add(course);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Erreur lors du traitement du cours", e);
                                    } finally {
                                        // Vérifier si tous les cours ont été traités
                                        if (coursesProcessed.incrementAndGet() == totalCourses) {
                                            callback.onCoursesLoaded(userCourses);
                                        }
                                    }
                                }

                                @Override
                                public void onError(String message) {
                                    Log.e(TAG, "Erreur lors du chargement du cours: " + message);
                                    // Même en cas d'erreur, incrémenter le compteur
                                    if (coursesProcessed.incrementAndGet() == totalCourses) {
                                        callback.onCoursesLoaded(userCourses);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors du traitement des cours de l'utilisateur", e);
                        callback.onError("Erreur: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Requête annulée", error.toException());
                    callback.onError(error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique", e);
            callback.onError("Erreur critique: " + e.getMessage());
        }
    }

    /**
     * Recherche des cours par titre ou description
     * @param query Texte à rechercher
     * @param callback Callback pour récupérer les résultats
     */
    public void searchCourses(String query, CoursesCallback callback) {
        try {
            // Validation de la requête de recherche
            if (query == null) {
                callback.onError("Requête de recherche invalide");
                return;
            }

            final String searchQuery = query.toLowerCase().trim();

            // Si la requête est vide, retourner tous les cours
            if (searchQuery.isEmpty()) {
                getAllCourses(callback);
                return;
            }

            coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        List<Course> courses = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                Course course = snapshot.getValue(Course.class);
                                if (course != null) {
                                    // Vérifier si le titre ou la description contient la requête
                                    boolean titleMatch = course.getTitle() != null &&
                                            course.getTitle().toLowerCase().contains(searchQuery);
                                    boolean descMatch = course.getDescription() != null &&
                                            course.getDescription().toLowerCase().contains(searchQuery);

                                    if (titleMatch || descMatch) {
                                        course.setCourseId(snapshot.getKey());
                                        courses.add(course);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Erreur lors du traitement d'un résultat de recherche", e);
                                // Continuer avec le cours suivant
                            }
                        }
                        callback.onCoursesLoaded(courses);
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de la recherche", e);
                        callback.onError("Erreur: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Requête annulée", databaseError.toException());
                    callback.onError(databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique lors de la recherche", e);
            callback.onError("Erreur critique: " + e.getMessage());
        }
    }

    /**
     * Met à jour le pourcentage de progression d'un cours pour un utilisateur
     * @param courseId ID du cours
     * @param userId ID de l'utilisateur
     * @param percentage Nouveau pourcentage de progression
     * @param callback Callback pour confirmer la mise à jour
     */
    public void updateCourseProgress(String courseId, String userId, int percentage,
                                     OnSuccessListener<Void> callback) {
        try {
            // Validation des paramètres
            if (courseId == null || courseId.isEmpty()) {
                throw new IllegalArgumentException("ID de cours invalide");
            }
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("ID utilisateur invalide");
            }
            if (percentage < 0 || percentage > 100) {
                throw new IllegalArgumentException("Pourcentage invalide: " + percentage);
            }

            DatabaseReference progressRef = FirebaseDatabase.getInstance()
                    .getReference("user_progress")
                    .child(userId)
                    .child(courseId);

            progressRef.child("percentage").setValue(percentage)
                    .addOnSuccessListener(callback)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la mise à jour de la progression", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique lors de la mise à jour de la progression", e);
            // L'appelant devrait gérer ce cas
        }
    }

    /**
     * Récupère la catégorie d'un cours
     * @param courseId ID du cours
     * @param listener Listener pour le résultat
     */
    private void getCourseCategory(String courseId, OnCategoryLoadedListener listener) {
        try {
            coursesRef.child(courseId).child("category").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String category = snapshot.getValue(String.class);
                            listener.onCategoryLoaded(category);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Requête annulée", error.toException());
                            listener.onCategoryLoaded(null);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la récupération de la catégorie", e);
            listener.onCategoryLoaded(null);
        }
    }

    /**
     * Récupère les cours des catégories spécifiées
     * @param categories Liste des catégories
     * @param callback Callback pour le résultat
     */
    private void getCoursesFromCategories(List<String> categories, CoursesCallback callback) {
        try {
            if (categories.isEmpty()) {
                // Si aucune catégorie, retourner des cours populaires
                getPopularCourses(callback);
                return;
            }

            List<Course> recommendedCourses = new ArrayList<>();
            AtomicInteger categoriesProcessed = new AtomicInteger(0);

            for (String category : categories) {
                coursesRef.orderByChild("category").equalTo(category)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try {
                                    for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                                        Course course = courseSnapshot.getValue(Course.class);
                                        if (course != null) {
                                            course.setCourseId(courseSnapshot.getKey());
                                            recommendedCourses.add(course);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Erreur lors du traitement des cours par catégorie", e);
                                } finally {
                                    // Vérifier si toutes les catégories ont été traitées
                                    if (categoriesProcessed.incrementAndGet() == categories.size()) {
                                        // Limiter à 10 cours recommandés
                                        if (recommendedCourses.size() > 10) {
                                            recommendedCourses.subList(10, recommendedCourses.size()).clear();
                                        }
                                        callback.onCoursesLoaded(recommendedCourses);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Requête annulée", error.toException());
                                if (categoriesProcessed.incrementAndGet() == categories.size()) {
                                    callback.onError(error.getMessage());
                                }
                            }
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique lors de la récupération par catégories", e);
            callback.onError("Erreur critique: " + e.getMessage());
        }
    }

    /**
     * Récupère les cours recommandés pour un utilisateur
     * @param userId ID de l'utilisateur
     * @param callback Callback pour le résultat
     */
    public void getRecommendedCourses(String userId, CoursesCallback callback) {
        try {
            DatabaseReference userProgressRef = FirebaseDatabase.getInstance().getReference("user_progress")
                    .child(userId);

            userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (!snapshot.exists() || !snapshot.hasChildren()) {
                            // Pas de progrès, retourner simplement les cours populaires
                            getPopularCourses(callback);
                            return;
                        }

                        // Collecter les catégories des cours suivis par l'utilisateur
                        List<String> userCategories = new ArrayList<>();
                        final AtomicInteger coursesProcessed = new AtomicInteger(0);
                        final int totalCourses = (int) snapshot.getChildrenCount();

                        for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                            String courseId = courseSnapshot.getKey();
                            if (courseId != null) {
                                // Obtenir la catégorie du cours
                                getCourseCategory(courseId, category -> {
                                    try {
                                        if (category != null && !userCategories.contains(category)) {
                                            userCategories.add(category);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Erreur lors du traitement de la catégorie", e);
                                    } finally {
                                        // Vérifier si toutes les catégories ont été collectées
                                        if (coursesProcessed.incrementAndGet() == totalCourses) {
                                            getCoursesFromCategories(userCategories, callback);
                                        }
                                    }
                                });
                            } else {
                                // Incrémenter le compteur même pour les cours sans ID
                                if (coursesProcessed.incrementAndGet() == totalCourses) {
                                    getCoursesFromCategories(userCategories, callback);
                                }
                            }
                        }

                        // Si aucun cours n'a été trouvé (snapshot vide), charger les cours populaires
                        if (totalCourses == 0) {
                            getPopularCourses(callback);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de la recommandation de cours", e);
                        // En cas d'erreur, retourner des cours populaires comme fallback
                        getPopularCourses(callback);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Requête annulée", error.toException());
                    callback.onError(error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique lors de la recommandation", e);
            // En cas d'erreur critique, essayer de retourner des cours populaires
            try {
                getPopularCourses(callback);
            } catch (Exception ex) {
                callback.onError("Erreur critique: " + e.getMessage());
            }
        }
    }

    /**
     * Récupère les cours par catégorie
     * @param category Catégorie des cours
     * @param callback Callback pour récupérer les résultats
     */
    public void getCoursesByCategory(String category, CoursesCallback callback) {
        try {
            Query query = coursesRef.orderByChild("category").equalTo(category);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        List<Course> courses = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Course course = snapshot.getValue(Course.class);
                            if (course != null) {
                                course.setCourseId(snapshot.getKey());
                                courses.add(course);
                            }
                        }
                        callback.onCoursesLoaded(courses);
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors du traitement des cours par catégorie", e);
                        callback.onError("Erreur: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Requête annulée", databaseError.toException());
                    callback.onError(databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique", e);
            callback.onError("Erreur critique: " + e.getMessage());
        }
    }

    /**
     * Ajoute un nouveau cours dans la base de données
     * @param course Cours à ajouter
     * @param callback Callback pour confirmer l'ajout
     */
    public void addCourse(Course course, OnSuccessListener<Void> callback) {
        try {
            String courseId = coursesRef.push().getKey();
            if (courseId != null) {
                course.setCourseId(courseId);
                coursesRef.child(courseId).setValue(course)
                        .addOnSuccessListener(callback)
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Erreur lors de l'ajout du cours", e);
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique lors de l'ajout du cours", e);
        }
    }

    /**
     * Met à jour un cours existant
     * @param course Cours avec les modifications
     * @param callback Callback pour confirmer la mise à jour
     */
    public void updateCourse(Course course, OnSuccessListener<Void> callback) {
        try {
            if (course.getCourseId() != null) {
                coursesRef.child(course.getCourseId()).setValue(course)
                        .addOnSuccessListener(callback)
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Erreur lors de la mise à jour du cours", e);
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique lors de la mise à jour du cours", e);
        }
    }

    /**
     * Supprime un cours
     * @param courseId ID du cours à supprimer
     * @param callback Callback pour confirmer la suppression
     */
    public void deleteCourse(String courseId, OnSuccessListener<Void> callback) {
        try {
            if (courseId == null || courseId.isEmpty()) {
                throw new IllegalArgumentException("ID de cours invalide");
            }

            coursesRef.child(courseId).removeValue()
                    .addOnSuccessListener(callback)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la suppression du cours", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur critique lors de la suppression du cours", e);
        }
    }
}