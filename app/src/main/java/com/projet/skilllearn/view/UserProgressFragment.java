package com.projet.skilllearn.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Achievement;
import com.projet.skilllearn.model.Course;
import com.projet.skilllearn.utils.UserProgressManager;
import com.projet.skilllearn.view.adapters.AchievementAdapter;
import com.projet.skilllearn.view.adapters.CourseProgressAdapter;
import com.projet.skilllearn.viewmodel.CourseViewModel;

import java.util.ArrayList;
import java.util.List;

public class UserProgressFragment extends Fragment {

    private RecyclerView rvCourseProgress;
    private RecyclerView rvAchievements;
    private TextView tvNoCourses;
    private TextView tvNoAchievements;
    private TextView tvUserName;
    private TextView tvTotalProgress;

    private UserProgressManager progressManager;
    private CourseViewModel courseViewModel;
    private CourseProgressAdapter courseAdapter;
    private AchievementAdapter achievementAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les vues
        rvCourseProgress = view.findViewById(R.id.rv_course_progress);
        rvAchievements = view.findViewById(R.id.rv_achievements);
        tvNoCourses = view.findViewById(R.id.tv_no_courses);
        tvNoAchievements = view.findViewById(R.id.tv_no_achievements);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvTotalProgress = view.findViewById(R.id.tv_total_progress);

        // Initialiser les adaptateurs
        courseAdapter = new CourseProgressAdapter(requireContext(), new ArrayList<>());
        achievementAdapter = new AchievementAdapter(requireContext(), new ArrayList<>());

        // Configurer les RecyclerViews
        rvCourseProgress.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCourseProgress.setAdapter(courseAdapter);

        rvAchievements.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        rvAchievements.setAdapter(achievementAdapter);

        // Initialiser le gestionnaire de progrès
        progressManager = UserProgressManager.getInstance();

        // Afficher le nom de l'utilisateur
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            if (userName != null && !userName.isEmpty()) {
                tvUserName.setText(userName);
            } else {
                tvUserName.setText(R.string.user_profile);
            }
        }

        // Charger les données
        loadUserCourses();
        loadUserAchievements();
    }

    /**
     * Charge les cours de l'utilisateur avec leur progression
     */
    private void loadUserCourses() {
        progressManager.getUserCourses(new UserProgressManager.UserCoursesCallback() {
            @Override
            public void onCoursesLoaded(List<String> courseIds) {
                if (courseIds.isEmpty()) {
                    rvCourseProgress.setVisibility(View.GONE);
                    tvNoCourses.setVisibility(View.VISIBLE);
                } else {
                    rvCourseProgress.setVisibility(View.VISIBLE);
                    tvNoCourses.setVisibility(View.GONE);

                    // Charger les détails des cours
                    List<Course> userCourses = new ArrayList<>();
                    for (String courseId : courseIds) {
                        // Utiliser le repository pour obtenir les détails du cours
                        courseViewModel.selectCourse(courseId);
                        courseViewModel.getSelectedCourse().observe(getViewLifecycleOwner(), course -> {
                            if (course != null) {
                                // Obtenir la progression
                                progressManager.getCourseProgress(courseId, new UserProgressManager.CourseProgressCallback() {
                                    @Override
                                    public void onProgressLoaded(int percentage) {
                                        course.setUserProgress(percentage);
                                        userCourses.add(course);

                                        // Mettre à jour l'adaptateur
                                        courseAdapter.updateCourses(userCourses);
                                        calculateTotalProgress(userCourses);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Charge les succès de l'utilisateur
     */
    private void loadUserAchievements() {
        progressManager.getUserAchievements(new UserProgressManager.AchievementsCallback() {
            @Override
            public void onAchievementsLoaded(List<Achievement> achievements) {
                if (achievements.isEmpty()) {
                    rvAchievements.setVisibility(View.GONE);
                    tvNoAchievements.setVisibility(View.VISIBLE);
                } else {
                    rvAchievements.setVisibility(View.VISIBLE);
                    tvNoAchievements.setVisibility(View.GONE);
                    achievementAdapter.updateAchievements(achievements);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Calcule la progression totale de l'utilisateur
     * @param courses Liste des cours de l'utilisateur
     */
    private void calculateTotalProgress(List<Course> courses) {
        if (courses.isEmpty()) {
            tvTotalProgress.setText("0%");
            return;
        }

        int totalProgress = 0;
        for (Course course : courses) {
            totalProgress += course.getUserProgress();
        }

        int averageProgress = totalProgress / courses.size();
        tvTotalProgress.setText(averageProgress + "%");
    }
}