// Créez CourseDetailActivity.java
package com.projet.skilllearn.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Course;
import com.projet.skilllearn.repository.CourseRepository;
import com.squareup.picasso.Picasso;

public class CourseDetailActivity extends AppCompatActivity {

    private ImageView ivCourseImage;
    private TextView tvCourseTitle, tvCourseInstructor, tvCourseDuration, tvCourseDescription;
    private RecyclerView rvTopics;
    private Button btnStartCourse;
    private ProgressBar progressBar;

    private CourseRepository repository;
    private String courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // Récupérer le courseId
        courseId = getIntent().getStringExtra("courseId");
        if (courseId == null) {
            Toast.makeText(this, "Erreur: Impossible de charger le cours", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        ivCourseImage = findViewById(R.id.iv_course_image);
        tvCourseTitle = findViewById(R.id.tv_course_title);
        tvCourseInstructor = findViewById(R.id.tv_course_instructor);
        tvCourseDuration = findViewById(R.id.tv_course_duration);
        tvCourseDescription = findViewById(R.id.tv_course_description);
        rvTopics = findViewById(R.id.rv_topics);
        btnStartCourse = findViewById(R.id.btn_start_course);
        progressBar = findViewById(R.id.progress_bar);

        // Configurer le RecyclerView
        rvTopics.setLayoutManager(new LinearLayoutManager(this));

        // Initialiser le repository
        repository = new CourseRepository();

        // Charger les détails du cours
        loadCourseDetails();

        // Configurer le bouton de démarrage
        btnStartCourse.setOnClickListener(v -> {
            // Implémenter le démarrage du cours
            Toast.makeText(this, "Démarrage du cours...", Toast.LENGTH_SHORT).show();
            // Naviguer vers l'activité de lecture du cours
        });
    }

    private void loadCourseDetails() {
        progressBar.setVisibility(View.VISIBLE);

        // Ici, vous devriez implémenter la méthode pour charger les détails du cours
        // depuis Firebase en utilisant courseId
        // ...
    }
}