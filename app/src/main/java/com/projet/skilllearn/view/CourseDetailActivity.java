package com.projet.skilllearn.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Course;
import com.projet.skilllearn.repository.CourseRepository;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.widget.Toolbar;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Activer le bouton de navigation vers le haut
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
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
        btnStartCourse.setOnClickListener(v -> startCourse());
    }

    private void loadCourseDetails() {
        progressBar.setVisibility(View.VISIBLE);

        // Vérifier que courseId est défini
        if (courseId == null || courseId.isEmpty()) {
            Toast.makeText(this, "Erreur: ID de cours invalide", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        Log.d("CourseDetail", "Chargement des détails du cours avec ID: " + courseId);

        // Référence au cours dans Firebase
        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses").child(courseId);

        courseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("CourseDetail", "Données du cours récupérées");

                    // Récupérer les valeurs directement
                    String title = snapshot.child("title").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String author = snapshot.child("author").getValue(String.class);
                    String authorName = snapshot.child("authorName").getValue(String.class);

                    // Valeurs numériques
                    Integer durationMinutes = null;
                    if (snapshot.hasChild("durationMinutes")) {
                        durationMinutes = snapshot.child("durationMinutes").getValue(Integer.class);
                    }

                    // URL de l'image
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    // Mettre à jour l'interface utilisateur
                    tvCourseTitle.setText(title != null ? title : "Titre non disponible");
                    tvCourseInstructor.setText("Instructeur: " + (authorName != null ? authorName : (author != null ? author : "Inconnu")));
                    tvCourseDuration.setText("Durée: " + (durationMinutes != null ? durationMinutes : 0) + " minutes");
                    tvCourseDescription.setText(description != null ? description : "Description non disponible");

                    // Charger l'image du cours si disponible
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // Utiliser Picasso ou Glide pour charger l'image
                        Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_course)
                                .error(R.drawable.error_course)
                                .into(ivCourseImage);
                    } else {
                        // Utiliser une image par défaut
                        ivCourseImage.setImageResource(R.drawable.placeholder_course);
                    }

                    // Si nécessaire, chargez également les sujets du cours (sections)
                    loadCourseSections(courseId);

                    // Activer le bouton de démarrage
                    btnStartCourse.setEnabled(true);

                } else {
                    Log.e("CourseDetail", "Cours introuvable dans Firebase");
                    Toast.makeText(CourseDetailActivity.this, "Cours introuvable", Toast.LENGTH_SHORT).show();
                    btnStartCourse.setEnabled(false);
                }

                // Masquer l'indicateur de progression
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CourseDetail", "Erreur lors du chargement du cours: " + error.getMessage());
                Toast.makeText(CourseDetailActivity.this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnStartCourse.setEnabled(false);
            }
        });
    }

    // Méthode pour charger les sections du cours
    private void loadCourseSections(String courseId) {
        DatabaseReference sectionsRef = FirebaseDatabase.getInstance().getReference("sections");

        sectionsRef.orderByChild("courseId").equalTo(courseId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> sectionTitles = new ArrayList<>();

                if (snapshot.exists()) {
                    // Parcourir toutes les sections
                    for (DataSnapshot sectionSnapshot : snapshot.getChildren()) {
                        String title = sectionSnapshot.child("title").getValue(String.class);
                        if (title != null) {
                            sectionTitles.add(title);
                        }
                    }

                    // Mettre à jour l'interface utilisateur avec les sections
                    if (!sectionTitles.isEmpty()) {
                        // Exemple : afficher dans un TextView
                        // tvTopics.setText(TextUtils.join("\n• ", sectionTitles));

                        // Ou configurer un RecyclerView avec un adaptateur
                        // SectionsAdapter adapter = new SectionsAdapter(sectionTitles);
                        // rvTopics.setAdapter(adapter);
                    } else {
                        // tvTopics.setText("Aucun sujet disponible");
                    }
                } else {
                    // tvTopics.setText("Aucun sujet disponible");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CourseDetail", "Erreur lors du chargement des sections: " + error.getMessage());
            }
        });
    }

    // Méthode pour démarrer le cours
    private void startCourse() {
        Log.d("CourseDetail", "Méthode startCourse() appelée");

        try {
            // Assurez-vous d'avoir l'ID du cours
            if (courseId == null || courseId.isEmpty()) {
                courseId = "course1"; // Utiliser une valeur par défaut pour le test
            }

            Log.d("CourseDetail", "ID du cours: " + courseId);

            // Utiliser un Intent avec le nom complet de la classe
            Intent intent = new Intent(CourseDetailActivity.this,
                    com.projet.skilllearn.view.CoursePlayerActivity.class);
            intent.putExtra("courseId", courseId);

            // Afficher un Toast de confirmation
            Toast.makeText(this, "Démarrage du cours...", Toast.LENGTH_SHORT).show();

            Log.d("CourseDetail", "Avant startActivity");
            startActivity(intent);
            Log.d("CourseDetail", "Après startActivity");

        } catch (Exception e) {
            Log.e("CourseDetail", "Exception lors du démarrage du cours", e);
            e.printStackTrace(); // Imprime la pile d'appels complète
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Gérer le clic sur le bouton de retour dans la toolbar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Assurez-vous que onBackPressed() est correctement implémenté

}