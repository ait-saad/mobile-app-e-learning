package com.projet.skilllearn.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.projet.skilllearn.model.Course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repository pour accéder et gérer les données des cours dans Firebase
 */
public class CourseRepository {
    private final DatabaseReference coursesRef;
    /**
     * Récupère les cours par niveau de difficulté
     * @param level Niveau de difficulté (débutant, intermédiaire, expert)
     * @param callback Callback pour le résultat
     */
    public void getCoursesByLevel(String level, CoursesCallback callback) {
        coursesRef.orderByChild("level").equalTo(level)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Course> courses = new ArrayList<>();
                        for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                            Course course = courseSnapshot.getValue(Course.class);
                            if (course != null) {
                                course.setCourseId(courseSnapshot.getKey());
                                courses.add(course);
                            }
                        }
                        callback.onCoursesLoaded(courses);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    /**
     * Récupère les cours triés par popularité
     * @param callback Callback pour le résultat
     */
    public void getCoursesByPopularity(CoursesCallback callback) {
        coursesRef.orderByChild("enrolledCount").limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    /**
     * Récupère les cours auxquels un utilisateur est inscrit
     * @param userId ID de l'utilisateur
     * @param callback Callback pour le résultat
     */
    public void getUserCourses(String userId, CoursesCallback callback) {
        DatabaseReference userProgressRef = FirebaseDatabase.getInstance().getReference("user_progress")
                .child(userId);

        userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    callback.onCoursesLoaded(new ArrayList<>());
                    return;
                }

                List<Course> userCourses = new ArrayList<>();
                AtomicInteger coursesProcessed = new AtomicInteger(0);
                int totalCourses = (int) snapshot.getChildrenCount();

                for (DataSnapshot courseProgress : snapshot.getChildren()) {
                    String courseId = courseProgress.getKey();
                    if (courseId != null) {
                        getCourseById(courseId, new CourseCallback() {
                            @Override
                            public void onCourseLoaded(Course course) {
                                // Ajouter la progression à l'objet cours
                                if (courseProgress.child("percentage").exists()) {
                                    Integer percentage = courseProgress.child("percentage").getValue(Integer.class);
                                    if (percentage != null) {
                                        course.setUserProgress(percentage);
                                    }
                                }

                                userCourses.add(course);

                                // Vérifier si tous les cours ont été traités
                                if (coursesProcessed.incrementAndGet() == totalCourses) {
                                    callback.onCoursesLoaded(userCourses);
                                }
                            }

                            @Override
                            public void onError(String message) {
                                if (coursesProcessed.incrementAndGet() == totalCourses) {
                                    callback.onCoursesLoaded(userCourses);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
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
     * Constructeur
     */
    public CourseRepository() {
        coursesRef = FirebaseDatabase.getInstance().getReference("courses");
    }

    /**
     * Récupère tous les cours dans la base de données
     * @param callback Callback pour récupérer les résultats
     */
    public void getAllCourses(CoursesCallback callback) {
        coursesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Course> courses = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        course.setCourseId(snapshot.getKey());
                        courses.add(course);
                    }
                }
                callback.onCoursesLoaded(courses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Récupère un cours par son ID
     * @param courseId ID du cours
     * @param callback Callback pour récupérer le résultat
     */
    public void getCourseById(String courseId, CourseCallback callback) {
        coursesRef.child(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Course course = snapshot.getValue(Course.class);
                if (course != null) {
                    course.setCourseId(snapshot.getKey());
                    callback.onCourseLoaded(course);
                } else {
                    callback.onError("Cours non trouvé");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Récupère les cours recommandés pour un utilisateur
     * @param userId ID de l'utilisateur
     * @param callback Callback pour le résultat
     */
    public void getRecommendedCourses(String userId, CoursesCallback callback) {
        DatabaseReference userProgressRef = FirebaseDatabase.getInstance().getReference("user_progress")
                .child(userId);

        userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Pas de progrès, retourner simplement les cours populaires
                    getPopularCourses(callback);
                    return;
                }

                // Collecter les catégories des cours suivis par l'utilisateur
                List<String> userCategories = new ArrayList<>();
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String courseId = courseSnapshot.getKey();
                    if (courseId != null) {
                        // Obtenir la catégorie du cours
                        getCourseCategory(courseId, category -> {
                            if (category != null && !userCategories.contains(category)) {
                                userCategories.add(category);
                            }

                            // Quand toutes les catégories sont collectées
                            if (userCategories.size() == snapshot.getChildrenCount()) {
                                // Charger les cours des catégories similaires
                                getCoursesFromCategories(userCategories, callback);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Récupère les cours populaires
     * @param callback Callback pour le résultat
     */
    private void getPopularCourses(CoursesCallback callback) {
        coursesRef.orderByChild("enrolledCount").limitToLast(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    /**
     * Récupère la catégorie d'un cours
     * @param courseId ID du cours
     * @param listener Listener pour le résultat
     */
    private void getCourseCategory(String courseId, OnCategoryLoadedListener listener) {
        coursesRef.child(courseId).child("category").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = snapshot.getValue(String.class);
                        listener.onCategoryLoaded(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onCategoryLoaded(null);
                    }
                });
    }

    /**
     * Interface pour récupérer les catégories
     */
    interface OnCategoryLoadedListener {
        void onCategoryLoaded(String category);
    }

    /**
     * Récupère les cours des catégories spécifiées
     * @param categories Liste des catégories
     * @param callback Callback pour le résultat
     */
    private void getCoursesFromCategories(List<String> categories, CoursesCallback callback) {
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
                            for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                                Course course = courseSnapshot.getValue(Course.class);
                                if (course != null) {
                                    course.setCourseId(courseSnapshot.getKey());
                                    recommendedCourses.add(course);
                                }
                            }

                            // Vérifier si toutes les catégories ont été traitées
                            if (categoriesProcessed.incrementAndGet() == categories.size()) {
                                // Limiter à 10 cours recommandés
                                if (recommendedCourses.size() > 10) {
                                    recommendedCourses.subList(10, recommendedCourses.size()).clear();
                                }

                                callback.onCoursesLoaded(recommendedCourses);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (categoriesProcessed.incrementAndGet() == categories.size()) {
                                callback.onError(error.getMessage());
                            }
                        }
                    });
        }
    }

    /**
     * Récupère les cours par catégorie
     * @param category Catégorie des cours
     * @param callback Callback pour récupérer les résultats
     */
    public void getCoursesByCategory(String category, CoursesCallback callback) {
        Query query = coursesRef.orderByChild("category").equalTo(category);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Course> courses = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        course.setCourseId(snapshot.getKey());
                        courses.add(course);
                    }
                }
                callback.onCoursesLoaded(courses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    /**
     * Ajoute un nouveau cours dans la base de données
     * @param course Cours à ajouter
     * @param callback Callback pour confirmer l'ajout
     */
    public void addCourse(Course course, OnSuccessListener<Void> callback) {
        String courseId = coursesRef.push().getKey();
        if (courseId != null) {
            course.setCourseId(courseId);
            coursesRef.child(courseId).setValue(course)
                    .addOnSuccessListener(callback)
                    .addOnFailureListener(e -> {
                        // Gérer l'erreur si nécessaire
                    });
        }
    }

    /**
     * Met à jour un cours existant
     * @param course Cours avec les modifications
     * @param callback Callback pour confirmer la mise à jour
     */
    public void updateCourse(Course course, OnSuccessListener<Void> callback) {
        if (course.getCourseId() != null) {
            coursesRef.child(course.getCourseId()).setValue(course)
                    .addOnSuccessListener(callback)
                    .addOnFailureListener(e -> {
                        // Gérer l'erreur si nécessaire
                    });
        }
    }

    /**
     * Supprime un cours
     * @param courseId ID du cours à supprimer
     * @param callback Callback pour confirmer la suppression
     */
    public void deleteCourse(String courseId, OnSuccessListener<Void> callback) {
        coursesRef.child(courseId).removeValue()
                .addOnSuccessListener(callback)
                .addOnFailureListener(e -> {
                    // Gérer l'erreur si nécessaire
                });
    }

    /**
     * Recherche des cours par titre
     * @param query Texte à rechercher
     * @param callback Callback pour récupérer les résultats
     */
    public void searchCourses(String query, CoursesCallback callback) {
        query = query.toLowerCase();
        String finalQuery = query;

        coursesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Course> courses = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        if (course.getTitle().toLowerCase().contains(finalQuery) ||
                                (course.getDescription() != null &&
                                        course.getDescription().toLowerCase().contains(finalQuery))) {
                            course.setCourseId(snapshot.getKey());
                            courses.add(course);
                        }
                    }
                }
                callback.onCoursesLoaded(courses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
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
        DatabaseReference progressRef = FirebaseDatabase.getInstance()
                .getReference("user_progress")
                .child(userId)
                .child(courseId);

        progressRef.child("percentage").setValue(percentage)
                .addOnSuccessListener(callback)
                .addOnFailureListener(e -> {
                    // Gérer l'erreur si nécessaire
                });
    }
}