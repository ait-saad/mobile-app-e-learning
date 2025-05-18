package com.projet.skilllearn.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projet.skilllearn.model.Course;
import com.projet.skilllearn.model.CourseSection;
import com.projet.skilllearn.repository.CourseRepository;
import com.projet.skilllearn.utils.UserProgressManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ViewModel pour gérer les opérations liées aux cours
 */
public class CourseViewModel extends ViewModel {
    private final CourseRepository repository;

    // LiveData pour les cours
    private final MutableLiveData<List<Course>> courses = new MutableLiveData<>();

    // LiveData pour le cours sélectionné
    private final MutableLiveData<Course> selectedCourse = new MutableLiveData<>();

    // LiveData pour l'état de chargement
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // LiveData pour les messages d'erreur
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Constructeur
     */
    public CourseViewModel() {
        repository = new CourseRepository();
    }

    /**
     * Obtient la liste des cours
     * @return LiveData contenant la liste des cours
     */
    public LiveData<List<Course>> getCourses() {
        return courses;
    }

    /**
     * Obtient le cours sélectionné
     * @return LiveData contenant le cours sélectionné
     */
    public LiveData<Course> getSelectedCourse() {
        return selectedCourse;
    }

    /**
     * Indique si une opération de chargement est en cours
     * @return LiveData contenant l'état de chargement
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Obtient les messages d'erreur
     * @return LiveData contenant le message d'erreur
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Définit le cours sélectionné
     * @param courseId ID du cours à sélectionner
     */
    public void selectCourse(String courseId) {
        Log.d("CourseViewModel", "Sélection du cours avec ID: " + courseId);
        isLoading.setValue(true);

        // Référence au cours dans la base de données
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses").child(courseId);

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Course course = snapshot.getValue(Course.class);
                    if (course != null) {
                        Log.d("CourseViewModel", "Cours chargé: " + course.getTitle());
                        course.setCourseId(courseId);

                        // Maintenant, charge les sections séparément
                        loadSectionsForCourse(course);
                    } else {
                        Log.e("CourseViewModel", "Cours non trouvé pour l'ID: " + courseId);
                        errorMessage.setValue("Cours non trouvé");
                        isLoading.setValue(false);
                    }
                } catch (Exception e) {
                    Log.e("CourseViewModel", "Erreur lors du chargement du cours", e);
                    errorMessage.setValue("Erreur: " + e.getMessage());
                    isLoading.setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CourseViewModel", "Chargement du cours annulé: " + error.getMessage());
                errorMessage.setValue(error.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    private void loadSectionsForCourse(@NonNull Course course) {
        Log.d("CourseViewModel", "Chargement des sections pour le cours: " + course.getTitle());

        // Requête pour obtenir les sections où courseId = course.getCourseId()
        DatabaseReference sectionsRef = FirebaseDatabase.getInstance().getReference("sections");
        sectionsRef.orderByChild("courseId").equalTo(course.getCourseId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<CourseSection> sections = new ArrayList<>();

                        for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                            try {
                                Log.d("CourseViewModel", "Section trouvée: " + sectionSnapshot.getKey());
                                CourseSection section = sectionSnapshot.getValue(CourseSection.class);
                                if (section != null) {
                                    sections.add(section);
                                }
                            } catch (Exception e) {
                                Log.e("CourseViewModel", "Erreur lors du chargement d'une section", e);
                            }
                        }

                        Log.d("CourseViewModel", "Nombre de sections chargées: " + sections.size());

                        // Tri par orderIndex
                        Collections.sort(sections, (s1, s2) ->
                                Integer.compare(s1.getOrderIndex(), s2.getOrderIndex()));

                        // Ajoute les sections au cours
                        course.setSections(sections);

                        // Mettre à jour le LiveData
                        selectedCourse.setValue(course);
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("CourseViewModel", "Chargement des sections annulé: " + error.getMessage());
                        errorMessage.setValue("Erreur lors du chargement des sections: " + error.getMessage());
                        isLoading.setValue(false);
                    }
                });
    }

    /**
     * Charge tous les cours depuis le repository
     */
    public void loadAllCourses() {
        isLoading.setValue(true);

        repository.getAllCourses(new CourseRepository.CoursesCallback() {
            @Override
            public void onCoursesLoaded(List<Course> courseList) {
                courses.setValue(courseList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Charge les cours d'une catégorie spécifique
     * @param category Catégorie des cours à charger
     */
    public void loadCoursesByCategory(String category) {
        isLoading.setValue(true);

        repository.getCoursesByCategory(category, new CourseRepository.CoursesCallback() {
            @Override
            public void onCoursesLoaded(List<Course> courseList) {
                courses.setValue(courseList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Recherche des cours par titre ou description
     * @param query Texte à rechercher
     */
    public void searchCourses(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadAllCourses();
            return;
        }

        isLoading.setValue(true);

        repository.searchCourses(query, new CourseRepository.CoursesCallback() {
            @Override
            public void onCoursesLoaded(List<Course> courseList) {
                courses.setValue(courseList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Met à jour un cours existant
     * @param course Cours avec les modifications
     */
    public void updateCourse(Course course) {
        isLoading.setValue(true);

        repository.updateCourse(course, unused -> {
            if (selectedCourse.getValue() != null &&
                    selectedCourse.getValue().getCourseId().equals(course.getCourseId())) {
                selectedCourse.setValue(course);
            }
            loadAllCourses();
            isLoading.setValue(false);
        });
    }

    /**
     * Supprime un cours
     * @param courseId ID du cours à supprimer
     */
    public void deleteCourse(String courseId) {
        isLoading.setValue(true);

        repository.deleteCourse(courseId, unused -> {
            if (selectedCourse.getValue() != null &&
                    selectedCourse.getValue().getCourseId().equals(courseId)) {
                selectedCourse.setValue(null);
            }
            loadAllCourses();
            isLoading.setValue(false);
        });
    }

    /**
     * Met à jour la progression d'un utilisateur pour un cours
     * @param courseId ID du cours
     * @param userId ID de l'utilisateur
     * @param percentage Pourcentage de progression
     */
    public void updateCourseProgress(String courseId, String userId, int percentage) {
        repository.updateCourseProgress(courseId, userId, percentage, unused -> {
            // Rafraîchir le cours sélectionné si nécessaire
            if (selectedCourse.getValue() != null &&
                    selectedCourse.getValue().getCourseId().equals(courseId)) {
                selectCourse(courseId);
            }
        });
    }

    /**
     * Réinitialise les erreurs
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    /**
     * À appeler lorsque l'utilisateur progresse dans un cours
     */
    public void onProgress(String courseId, int percentage) {
        // Mettre à jour la progression
        UserProgressManager.getInstance().updateCourseProgress(courseId, percentage);
    }

    /**
     * Charge les cours recommandés pour un utilisateur
     * @param userId ID de l'utilisateur
     */
    public void loadRecommendedCourses(String userId) {
        isLoading.setValue(true);

        repository.getRecommendedCourses(userId, new CourseRepository.CoursesCallback() {
            @Override
            public void onCoursesLoaded(List<Course> courseList) {
                courses.setValue(courseList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Filtre les cours par niveau de difficulté
     * @param level Niveau de difficulté ("débutant", "intermédiaire", "expert")
     */
    public void filterCoursesByLevel(String level) {
        isLoading.setValue(true);

        repository.getCoursesByLevel(level, new CourseRepository.CoursesCallback() {
            @Override
            public void onCoursesLoaded(List<Course> courseList) {
                courses.setValue(courseList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Trie les cours par durée
     * @param ascending Si vrai, tri en ordre croissant; sinon, décroissant
     */
    public void sortCoursesByDuration(boolean ascending) {
        List<Course> currentCourses = courses.getValue();
        if (currentCourses != null) {
            if (ascending) {
                currentCourses.sort((c1, c2) -> Integer.compare(c1.getDurationMinutes(), c2.getDurationMinutes()));
            } else {
                currentCourses.sort((c1, c2) -> Integer.compare(c2.getDurationMinutes(), c1.getDurationMinutes()));
            }
            courses.setValue(currentCourses);
        }
    }

    /**
     * Trie les cours par popularité (basé sur le nombre d'inscrits)
     */
    public void sortCoursesByPopularity() {
        isLoading.setValue(true);

        repository.getCoursesByPopularity(new CourseRepository.CoursesCallback() {
            @Override
            public void onCoursesLoaded(List<Course> courseList) {
                courses.setValue(courseList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    /**
     * Obtient les cours en cours d'un utilisateur
     * @param userId ID de l'utilisateur
     */
    public void getUserCourses(String userId) {
        isLoading.setValue(true);

        repository.getUserCourses(userId, new CourseRepository.CoursesCallback() {
            @Override
            public void onCoursesLoaded(List<Course> courseList) {
                courses.setValue(courseList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }
}